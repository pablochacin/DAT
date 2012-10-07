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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import ow.messaging.InetMessagingAddress;
import ow.messaging.Message;
import ow.messaging.MessageSender;
import ow.messaging.MessagingAddress;
import ow.util.AlarmClock;
import ow.util.Timer;
import ow.util.concurrent.SingletonThreadPoolExecutors;
import ow.util.concurrent.ExecutorBlockingMode;

public class TCPMessageSender implements MessageSender {
	private final static Logger logger = Logger.getLogger("messaging");

	private final TCPMessageReceiver receiver;

	protected TCPMessageSender(TCPMessageReceiver receiver) {
		this.receiver = receiver;
	}

	public void send(MessagingAddress dest, Message msg) throws IOException {
		this.adjustLoopbackAddress((InetMessagingAddress)dest);

		// destination is local
		MessagingAddress selfAddress = this.receiver.getSelfAddress();
		if (dest.equals(selfAddress)) {
			this.receiver.processMessage(msg);
			this.receiver.postProcessMessage(msg);
			return;
		}

		// destination is remote
		SocketAddress sockAddr = ((InetMessagingAddress)dest).getInetSocketAddress();
		SocketChannel sock = null;

		int retryCount = 0;
		while (true) {
			
				sock = this.receiver.connPool.get(sockAddr);

			try {
				this.send(sock, dest, msg);

				break;
			}
			catch (ClosedChannelException e) {
				// sock is stale. retry once.
				if (retryCount <= 0) {
					retryCount++;
					continue;
				}
				else {
					throw e;
				}
			}
		}

		this.receiver.connPool.put(sockAddr, sock);
	}

	private void send(SocketChannel sock, MessagingAddress dest, Message msg)  throws IOException {

		// set signature
		byte[] sig = this.receiver.provider.getMessageSignature();
		msg.setSignature(sig);

			ByteBuffer buf = Message.encode(sock, msg);

	}

	public Message sendAndReceive(MessagingAddress dest, final Message msg)
			throws IOException {
		this.adjustLoopbackAddress((InetMessagingAddress)dest);

		Message ret = null;

		// destination is local
		MessagingAddress selfAddress = this.receiver.getSelfAddress();
		if (dest.equals(selfAddress)) {
			ret = this.receiver.processMessage(msg);

			if (this.receiver.extMessageHandlerRegistered) {
				Runnable r = new Runnable () {
					public void run() {
						TCPMessageSender.this.receiver.postProcessMessage(msg);
					}
				};

				if (TCPMessageSender.this.receiver.config.getUseThreadPool()) {
					SingletonThreadPoolExecutors.getThreadPool(
							ExecutorBlockingMode.CONCURRENT_NON_BLOCKING, false).submit(r);
				}
				else {
					Thread t = new Thread(r);
					t.setName("TCPMessageSender: post-processing thread");
					t.setDaemon(false);
					t.start();
				}
			}

			return ret;
		}

		// destination is remote
		SocketAddress sockAddr = ((InetMessagingAddress)dest).getInetSocketAddress();
		SocketChannel sock = null;

		int retryCount = 0;
		while (true) {
			// prepare socket
				sock = this.receiver.connPool.get(sockAddr);

			// receive and dispose remaining data
			ByteBuffer buf = ByteBuffer.allocate(1024);
			sock.configureBlocking(false);
			while (true) {
				int read = sock.read(buf);
				if (read <= 0) break;
				buf.clear();

			}
			sock.configureBlocking(true);

			// send
			try {
				this.send(sock, dest, msg);
			}
			catch (ClosedChannelException e) {
				// sock is stale. retry once.
				if (retryCount <= 0) {
					retryCount++;
					continue;
				}
				else {
					throw e;
				}
			}

			// receive
			long timeout = this.receiver.provider.getTimeoutCalculator().calculateTimeout(dest);

			try {
				long start = Timer.currentTimeMillis();

				AlarmClock.setAlarm(timeout);

				try {
					ret = Message.decode(sock);
				}
				catch (IOException e) {
					// sock has been closed by the receiver.
					try { sock.close(); } catch (IOException e1) {}

					if (retryCount <= 0) {
						retryCount++;
						continue;
					}
					else {
						throw e;
					}
				}
				finally {
					AlarmClock.clearAlarm();
				}

				this.receiver.connPool.put(sockAddr, sock);

				// timeout calculation
				this.receiver.provider.getTimeoutCalculator().updateRTT(dest, (int)(Timer.currentTimeMillis() - start));
			}
			catch (Exception e) {	// interrupted by alarm
				Thread.interrupted();

				throw new IOException("Timeout: " + timeout + " msec.");
			}

			break;
		}

		return ret;
	}

	private void adjustLoopbackAddress(InetMessagingAddress dest) {
		// adjust loopback address (e.g. 127.0.0.1) to a real address
		if (dest.getInetAddress().isLoopbackAddress()) {
			dest.setInetAddress(((InetMessagingAddress)this.receiver.getSelfAddress()).getInetAddress());

		}
	}
}
