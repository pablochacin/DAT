package dat.network.socket;

import java.io.IOException;
import java.net.UnknownHostException;

import ow.messaging.MessageHandler;
import ow.messaging.MessageReceiver;
import ow.messaging.MessageSender;
import ow.messaging.MessagingAddress;
import ow.messaging.MessagingProvider;


import dat.DatException;
import dat.Message;
import dat.network.NodeAddress;
import dat.network.Transport;
import dat.network.TransportException;
import dat.network.TransportHandler;

public class SocketTransport implements Transport, MessageHandler {

	/**
	 * Transport handler initialized with a dummy handler.
	 */
	protected TransportHandler handler  = new TransportHandler(){
												public void dispatchMessage(Message message) {}

												@Override
												public void handleException(
														Throwable e) {
													
												}		
											   };						
								
											
	MessageSender sender;
												
	MessageReceiver receiver;
	
	MessagingProvider provider;
	
	MessagingAddress addr;
						
		
	public SocketTransport(MessagingProvider provider,	MessageSender sender,MessageReceiver receiver) {
		super();		

		this.provider = provider;
		this.sender = sender;
		this.receiver = receiver;
		this.addr=receiver.getSelfAddress();
	}

	@Override
	public void connect() throws DatException {

		receiver.addHandler(this);
	}

	@Override
	public void disconnect() throws DatException {
		
		receiver.removeHandler(this);
	}

	@Override
	public NodeAddress getAddress() {
		return new SocketAddress(addr);
	}

	@Override
	public void sendNetworkMessage(NodeAddress destination, Message message)
			throws TransportException {
		
		ow.messaging.Message msg = new ow.messaging.Message(addr,0,message);
		
		try {
			sender.send(((SocketAddress)destination).getMessagingAddress(),msg);
		} catch (IOException e) {
			throw new TransportException("Exception sending message to " + destination.getLocation() +" :"+e.getMessage());
		}
		
	}

	@Override
	public void setHandler(TransportHandler handler) {
		this.handler = handler;
		
	}

	@Override
	public ow.messaging.Message process(ow.messaging.Message msg){
			handler.dispatchMessage((Message)msg.getContents()[0]);
			return null;
	}

	@Override
	public NodeAddress resolve(String address) throws TransportException {
		try {
			return new SocketAddress(provider.getMessagingAddress(address));
		} catch (UnknownHostException e) {
			throw new TransportException("Exception resolving address [" + address + "]",e);
		}
	}

	@Override
	public void handleException(Throwable e) {
		handler.handleException(e);
		
	}



}
