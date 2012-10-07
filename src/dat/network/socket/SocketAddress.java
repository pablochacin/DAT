package dat.network.socket;

import java.util.UUID;

import ow.messaging.MessagingAddress;

import dat.network.NodeAddress;

public class SocketAddress implements NodeAddress {

	private static final long serialVersionUID = 1L;

	private MessagingAddress address;	
	
	private UUID ID;
	
	
	
	public SocketAddress(MessagingAddress address) {
		super();
		this.address = address;
		this.ID = UUID.nameUUIDFromBytes(address.toString().getBytes());
	}

	@Override
	public UUID getID() {
		return ID;
	}

	@Override
	public String getLocation() {
		return address.getHostAddress()+":"+address.getPort();
	}

	@Override
	public int compareTo(NodeAddress arg0) {
		return getLocation().compareTo(arg0.getLocation());
	}

	public MessagingAddress getMessagingAddress() {
		return this.address;
	}

	@Override
	public boolean equals(Object a){
		return this.getLocation().equals(((NodeAddress)a).getLocation());
	}
}
