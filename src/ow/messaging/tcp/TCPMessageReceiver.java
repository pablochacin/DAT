/*
 * Copyright 2006-2010 National Institute of Advanced Industrial Science
 * and Technology (AIST), and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ow.messaging.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import dat.utils.NetworkUtils;

import ow.messaging.ExtendedMessageHandler;
import ow.messaging.InetMessagingAddress;
import ow.messaging.Message;
import ow.messaging.MessageHandler;
import ow.messaging.MessageReceiver;
import ow.messaging.MessageSender;
import ow.messaging.MessagingAddress;
import ow.messaging.Signature;
import ow.util.concurrent.SingletonThreadPoolExecutors;
import ow.util.concurrent.ExecutorBlockingMode;

public class TCPMessageReceiver implements MessageReceiver, Runnable {


	private InetMessagingAddress selfAddr; 
	private ServerSocketChannel servSock;
	protected TCPMessagingConfiguration config;
	protected TCPMessagingProvider provider;
	protected ConnectionPool connPool;
	private Thread receiverThread;
	private Set<Thread> handlerThreads = Collections.synchronizedSet(new HashSet<Thread>());

	private List<MessageHandler> handlerList = new ArrayList<MessageHandler>();
	protected boolean extMessageHandlerRegistered = false;


	private static boolean oomPrinted = false;

	protected TCPMessageReceiver(InetAddress selfInetAddr, int port, int portRange,
			TCPMessagingConfiguration config, TCPMessagingProvider provider) throws IOException {
		this.config = config;
		this.provider = provider;

		// prepare a server socket
		this.servSock = ServerSocketChannel.open();

		// prepare local address
		if (selfInetAddr == null) {
			selfInetAddr = InetAddress.getLocalHost();
		}

		// bind to the specified address, and then to a local address if failed.
		ServerSocket s = this.servSock.socket();
		s.setReuseAddress(true);	// for development

		this.selfAddr = this.bind(s, selfInetAddr, port, portRange);

		if (this.selfAddr == null && !selfInetAddr.equals(InetAddress.getLocalHost())) {
			InetMessagingAddress boundAddr = this.bind(s, InetAddress.getLocalHost(), port, portRange);
			if (boundAddr != null) {
				boundAddr.setInetAddress(selfInetAddr);
				this.selfAddr = boundAddr;
			}
		}

		if (this.selfAddr == null) {
			String addrPort = selfInetAddr.getHostAddress() + ":" + port + "-" + (port + portRange - 1);
			throw new IOException("Could not bind to port: " + addrPort);
		}

		this.connPool = new ConnectionPool(
				config.getConnectionPoolSize(), config.getSenderKeepAliveTime());

	}

	private InetMessagingAddress bind(
			ServerSocket sock, InetAddress inetAddr, int fromPort, int range) throws IOException, NoSuchElementException {
		InetMessagingAddress addr = null;
		boolean bound = false;

		int port = NetworkUtils.getNextAvailable(fromPort, range);
		
		addr = new InetMessagingAddress(inetAddr,port);

		sock.bind(addr.getInetSocketAddress());

		return addr;
	}

	public MessagingAddress getSelfAddress() { return this.selfAddr; }

	public void setSelfAddress(String hostOrIP) throws UnknownHostException{
			this.selfAddr = this.provider.getMessagingAddress(
					hostOrIP, this.selfAddr.getPort());
	}

	public int getPort() { return this.selfAddr.getPort(); }

	public MessageSender getSender() {
		// does not share a sender
		return new TCPMessageSender(this);
	}

	public void start() {
		synchronized (this) {
			if (receiverThread == null) {
				receiverThread = new Thread(this);
				receiverThread.setDaemon(true);
				receiverThread.setName("TCPMessageReceiver");

				// give higher priority
				receiverThread.setPriority(Thread.currentThread().getPriority()
						+ this.config.getReceiverThreadPriority());

				receiverThread.start();
			}
		}
	}

	public void stop() {
		synchronized (this) {
			if (this.receiverThread != null) {
				this.receiverThread.interrupt();
				this.receiverThread = null;
			}
		}

		Thread[] handlerArray = new Thread[this.handlerThreads.size()];
		this.handlerThreads.toArray(handlerArray);

		for (int i = 0; i < handlerArray.length; i++) {
			handlerArray[i].interrupt();
		}

		this.handlerThreads.clear();


		// close all sockets in the connection pool
		this.connPool.clear();
	}

	public void run() {
		while (true) {
			SocketChannel sock = null;
			try {
				sock = servSock.accept();
			}
			catch (IOException e) {
				for(MessageHandler h: handlerList){
					h.handleException(e);
				}
			}

			// invoke a Thread handling an incoming Message
			Runnable r = new TCPMessageHandler(sock);

				if (this.config.getUseThreadPool()) {
					SingletonThreadPoolExecutors.getThreadPool(
							ExecutorBlockingMode.CONCURRENT_NON_BLOCKING, false).submit(r);
				}
				else {
					Thread handlerThread = new Thread(r);
					handlerThread.setDaemon(false);

					handlerThreads.add(handlerThread);

					handlerThread.start();
				}
		}
	}

	public void addHandler(MessageHandler handler) {
		List<MessageHandler> newHandlerList = new ArrayList<MessageHandler>();

		synchronized (this) {
			newHandlerList.addAll(this.handlerList);	// copy
			newHandlerList.add(handler);

			this.handlerList = newHandlerList;	// substitute
		}

		if (handler instanceof ExtendedMessageHandler) {
			this.extMessageHandlerRegistered = true;
		}
	}

	public void removeHandler(MessageHandler handler) {
		List<MessageHandler> newHandlerList = new ArrayList<MessageHandler>();

		synchronized (this) {
			newHandlerList.addAll(this.handlerList);	// copy
			newHandlerList.remove(handler);

			this.handlerList = newHandlerList;	// substitute
		}

		boolean exists = false;
		for (MessageHandler h: newHandlerList) {
			if (h instanceof ExtendedMessageHandler) {
				exists = true;
				break;
			}
		}
		this.extMessageHandlerRegistered = exists;
	}

	private class TCPMessageHandler implements Runnable {
		SocketChannel sock;

		TCPMessageHandler(SocketChannel sock) { this.sock = sock; }

		public void run() {
			Thread th = Thread.currentThread();
			String origName = th.getName();
			th.setName("TCPMessageHandler: " + this.sock.socket().getInetAddress());

			int times = 0;	// # of times a message is received on this socket.
			while (!Thread.interrupted()) {
				times++;

				long timeout = -1L;
				if (times > 1) timeout = config.getReceiverKeepAliveTime();

				Message msg = null;
				try {
					msg = Message.decode(this.sock, timeout);
				}
				catch (Exception e0) {
	
					// close Socket
					try { this.sock.close(); } catch (IOException e1) {}

					break;
				}

				// check signature
				byte[] sig = msg.getSignature();
				byte[] acceptableSig = TCPMessageReceiver.this.provider.getMessageSignature();
				if (!Signature.match(sig, acceptableSig))
					continue;

				// process the received message
				Message ret = TCPMessageReceiver.this.processMessage(msg);

				// return a Message (from the last handler)
				if (ret != null) {
					MessagingAddress src = msg.getSource();
					
					try {
						ByteBuffer buf = Message.encode(sock, ret);


					}
					catch (IOException e) {
						// close Socket
						try { sock.close(); } catch (IOException e1) {}

	
						break;
					}
				}

				// post-process
				TCPMessageReceiver.this.postProcessMessage(msg);
			}	// while (true)

			handlerThreads.remove(Thread.currentThread());

			th.setName(origName);
		}
	}

	protected Message processMessage(Message msg) {
		// call every handlers
		List<MessageHandler> currentHandlerList;
		synchronized (this) {
			currentHandlerList = handlerList;
		}

		Message ret = null;

		for (MessageHandler handler: currentHandlerList) {
			try {
				ret = handler.process(msg);
			}
			catch (Throwable e) {
				}
		}

		return ret;
	}

	protected void postProcessMessage(Message msg) {
		if (!this.extMessageHandlerRegistered) return;

		// call every handlers
		List<MessageHandler> currentHandlerList;
		synchronized (this) {
			currentHandlerList = handlerList;
		}

		for (MessageHandler handler: currentHandlerList) {
			if (!(handler instanceof ExtendedMessageHandler)) continue;

			try {
				((ExtendedMessageHandler)handler).postProcess(msg);
			}
			catch (Throwable e) {
			}
		}
	}
}
