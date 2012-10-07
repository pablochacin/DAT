/**
 * 
 */
package dat.network.socket;

import java.io.IOException;
import java.security.NoSuchProviderException;

import org.apache.commons.configuration.Configuration;

import ow.messaging.MessageReceiver;
import ow.messaging.MessageSender;
import ow.messaging.MessagingFactory;
import ow.messaging.MessagingProvider;
import ow.messaging.Signature;
import dat.network.Network;
import dat.network.NetworkException;
import dat.network.Transport;

/**
 * Implements a Network supported by a simple messaging communication transport.
 * This messaging is based on OverlayWeaver's messaging library 
 * (http://overlaywaver.sourceforge.net)
 * 
 * 
 * @author Pablo Chacin
 *
 */
public class SocketNetwork implements Network {

	MessagingProvider msgSrv;
	
	MessageSender sender;
	
	MessageReceiver receiver;
			

	@Override
	public Transport getTransport() {
		return new SocketTransport(msgSrv,sender,receiver);
	}

	@Override
	public void init(Configuration config) throws NetworkException {
		try {
			msgSrv = MessagingFactory.getProvider(config.getString("socket.transport","TCP"), 
					                       Signature.getAllAcceptingSignature());
			//msgSrv = new TCPMessagingProvider();
			msgSrv.setSelfAddress(config.getString("socket.address","localhost"));		
			receiver = msgSrv.getReceiver(msgSrv.getDefaultConfiguration(), config.getInt("socket.port"), config.getInt("socket.range"));
			receiver.start();
			sender = receiver.getSender();

		} catch (IOException e) {
		  throw new NetworkException("Exception initializing messaging",e);
		} catch (NoSuchProviderException e) {
			throw new NetworkException("Invalid transport: "+config.getString("messaging.transport"),e);
		}
	}

}
