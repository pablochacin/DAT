package dat.core;


import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import dat.Algorithm;
import dat.DatException;
import dat.Event;
import dat.Message;
import dat.Node;
import dat.network.NodeAddress;
import dat.network.TransportException;
import dat.utils.ReflectionUtils;
import dat.utils.TypedMap;

/**
 * 
 * Controls the execution of an Algorithm, isolating from other algorithms.
 * 
 * Executes in its own {@link Thread}
 * 
 * @author 
 *
 */
public class AlgorithmNode implements Node, Runnable {


	/**
	 * Any work pending to be processed by the algorithm
	 * 
	 */
	private enum WorkElementType {Message,Event};

	private class WorkElement {

		
		private WorkElementType type;
		

		private Object element;


		public WorkElement(WorkElementType type, Object element) {
			this.type = type;
			this.element = element;
		}

		public WorkElementType getType() {
			return type;
		}

		public Object getElement() {
			return element;
		}


	}

	private static Random rand = new Random();
	
	/**
	 * Node on which this algorithm is executed
	 */
	private NetworkNode networkNode;

	/**
	 * Algorithm
	 */
	private Algorithm algorithm;

	/**
	 * Queue of work for this 
	 */
	private BlockingQueue<WorkElement> workQueue;
	
	/**
	 * Name under which this algorithm is registered
	 */
	private String name;

	private Configuration parameters;
	
	private Logger log;
		
	/**
	 * Constructor 
	 * 
	 * @param networkNode
	 * @param name a String with the name under which 
	 * @param algorithm
	 */
	public AlgorithmNode(NetworkNode networkNode,String name,Configuration config) {
		super();
		this.networkNode = networkNode;
		this.name = name;
		
		String algClass = config.getString("class");
		
		try {
			this.algorithm = (Algorithm) Class.forName(algClass).newInstance();
			
			log = Logger.getLogger("dat.node.algorithm."+name);
			String logLevel = config.getString("loglevel");
			if(logLevel != null){
				log.setLevel(Level.toLevel(logLevel));
			}
			
			Configuration parameters = config.subset("param");
			
			this.parameters = parameters;
			this.workQueue = new LinkedBlockingQueue<WorkElement>();
		} catch (Exception e) {
			throw new IllegalArgumentException("Exception instantiating algorithm",e);
		}



	}


	public void signalEvent(Event event){

		workQueue.add(new WorkElement(WorkElementType.Event,event));		

	}
	
	
	public void signalMessage(Message message){

		try{
			workQueue.add(new WorkElement(WorkElementType.Message,message));
		}catch(IllegalStateException e){
			log.warn("Discarting message due to lack of capacity \n" + message.toString());
		}

	}
	
	@Override
	public NodeAddress getAddress() {
		return networkNode.getAddress();
	}


	@Override
	public long getTime() {
		return networkNode.getTime();
	}
	


	@Override
	public void scheduleEvent(String id,long delay, String type, TypedMap attributes) {
		scheduleEvent(new Event(id,delay,type,attributes));
	}

	@Override
	public void scheduleEvent(String id,long delay, String type) {
		scheduleEvent(id,delay,type,new TypedMap());
	}

	@Override
	public void scheduleEvent(Event event){
		
		if(event.getTime() <= 0){
			throw new IllegalArgumentException("Delay must be greater than 0");
		}
		
		networkNode.scheduleEvent(this,event);
	}
	
	@Override
	public void cancelEvent(String id){
	
		networkNode.cancelEvent(id);
	}
	
	
	
	public void sendMessage(NodeAddress destination, Message message) throws DatException {
		
		if(destination == null){
			throw new IllegalArgumentException("Destination can't be null");
		}
		
		if(message == null){
			throw new IllegalArgumentException("Message can't be null");
		}
		
		message.setDestination(destination);
		message.setSender(getAddress());
		message.setAlgorithm(getName());
		
		sendNetworkMessage(destination, message);
	}


	
	@Override
	public void sendMessage(NodeAddress destination,String id,String type, TypedMap attributes) throws DatException {
		
		Message message = new Message(id,getName(),type,getAddress(),destination,attributes);
		
		sendMessage(destination, message);

		
	}

	
	@Override
	public void sendMessage(List<NodeAddress>  destination, String id,String type, TypedMap attributes) throws DatException {
		
		for(NodeAddress d: destination){
			sendMessage(d,id,type,attributes);
		}
	}

	@Override
	public void sendMessage(List<NodeAddress>  destination, Message message) throws DatException {
		
		for(NodeAddress d: destination){
			sendMessage(d,message);
		}
	}

	
	private void sendNetworkMessage(NodeAddress destination,Message message) throws DatException{
				
		log.debug("Sending message " + message.toString());
		networkNode.sendNetworkMessage(destination, message);
	}
	


	
	@Override
	public UUID getID(){
		return networkNode.getID();
	}

	@Override
	public Object getAlgorithm(String name, Class... interfaces) {
		return networkNode.getAlgorithm(name,interfaces);
	}


	/**
	 * Returns the name under which the {@link Algorithm} was registered (see {@link DAT#registerAlgorithm(String, Algorithm)}
	 * 
	 * @return the name associated which this algorithm.
	 */

	public String getName(){
		return name;
	}
	

	@Override
	public  Configuration getParameters(){
		return parameters;
	}
	
	
	@Override
	public synchronized void run() {
		
		DAT.setAlgorithmNode(this);
		MDC.put("context", name);
		
		algorithm.init(this);
		
		synchronized (algorithm) {
			algorithm.notify();
		}


		while(true){

			try {
				WorkElement e =  workQueue.take();
				
				log.debug("Processing " + e.type.name() + " " + e.getElement().toString());
				
				//dynamically find the method to execute based on the event type and
				//the argument's class
				String methodName = "handle"+e.type.name();
				Object arg = e.getElement();
				
				ReflectionUtils.invoke(methodName,algorithm,arg);
				

			} catch (InterruptedException e) {
				log.trace("Interrupted while processing work queue");
				return;
			} catch (Exception e) {
				log.error("Exception processing work element",e);
			} 


		}


	}

	
	NetworkNode netNetworkNode(){
		return networkNode;
	}

	Algorithm getAlgorithm(){
		return this.algorithm;
	}


	@Override
	public Logger getLog() {
		return log;
	}


	@Override
	public NodeAddress resolve(String address) throws DatException {
		try {
			return networkNode.resolve(address);
		} catch (TransportException e) {
			throw new DatException(e);
		}
	}






}
