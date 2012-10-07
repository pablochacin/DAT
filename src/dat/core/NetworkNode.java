package dat.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import dat.Algorithm;
import dat.DatException;
import dat.Event;
import dat.Message;
import dat.network.Network;
import dat.network.NodeAddress;
import dat.network.Transport;
import dat.network.TransportException;
import dat.network.TransportHandler;
import dat.utils.Exponential;
import dat.utils.FormattingUtils;

/**
 * A node in a network. Handles the communication with other nodes.
 * 
 * Receives the following configuration parameters:
 * <ul>
 * <li> node.failure.delay: mean time between failures. Distribution is assumed to be Exponential.
 *                          a value of 0 means no failure.
 * <li> node.failure.recovery: mean time before recovering from a failure. Distribution is assumed to be Exponential.                         
 * </ul>
 * 
 * 
 * @author Pablo Chacin
 *
 */
public class NetworkNode implements Runnable, TransportHandler {

	/**
	 * Sets the environment to execute an application thread
	 * 
	 * @author Pablo
	 *
	 */
	private class AppThread implements Runnable{

		Runnable app;
		
		public AppThread(Runnable app){
			this.app = app;
		}
		
		@Override public void run(){
			MDC.put("context", "application");			
			app.run();
		}
	}
	
	protected Logger log = Logger.getLogger("dat.node");

	protected Map<String,AlgorithmNode> algorithms;
	
	protected List<AlgorithmNode> installList;

	protected Map<String,EventTask> events;

	protected Timer scheduler;

	protected Transport transport;

	protected Network network;
	

	/**
	 * 
	 */
	protected ThreadGroup threads;

	/**
	 * Application to launch in this node
	 */
	protected Runnable application;

	/**
	 * Mean time between failures
	 */
	protected long failureDelay;

	/**
	 * Mean time before recovering from a failure
	 */
	protected long recoveryDelay = 1;


	/**
	 * Parameters from configuration
	 */
	protected Configuration configuration;

	protected Configuration appConfiguration;


	/**
	 * TimerTask to trigger an {@link Event}
	 * 
	 *
	 */
	protected class EventTask extends TimerTask {

		AlgorithmNode context;

		Event event;

		public EventTask(AlgorithmNode context, Event event) {
			super();
			this.context = context;
			this.event = event;
		}


		@Override
		public void run() {

			context.signalEvent(event);	
			events.remove(event.getId());
		}


	}
	
	/**
	 * Task scheduled on an application. 
	 * 
	 * TODO: Currently, not used because the task would be executed in the 
	 * Timer's thread and not in the application's thread.
	 * 
	 * @author Pablo Chacin
	 *
	 */
	protected class ApplicationTask extends TimerTask{

		private Runnable target;

		protected ApplicationTask(Runnable target){
			super();
			this.target = target;
		}

		@Override
		public void run() {
			target.run();

		}

	}


	/**
	 * Constructor. Can be created only from other classes in the package, in particular DAT
	 * 
	 * @param configuration
	 * @param network
	 * @param application
	 * @param appConfiguration
	 */
	NetworkNode(Configuration configuration,Network network,Runnable application,Configuration appConfiguration) {
		this.configuration = configuration;		
		this.network  = network;
		this.transport = network.getTransport();
		this.application = application;
		this.appConfiguration = appConfiguration;
		this.algorithms = new HashMap<String, AlgorithmNode>();
		this.installList = new ArrayList<AlgorithmNode>();
		this.scheduler = new Timer();
		this.events = new HashMap<String,EventTask>();
		}



	/**
	 * 
	 * @return the configuration for the application
	 */
	Configuration getAppParameters(){
		return appConfiguration;
	}


	/**
	 * Returns the current execution time. 
	 * 
	 * Currently, this is the real execution time, future versions will support simulated 
	 * execution time to slow dawn execution for debugging and educational purposes.
	 * 
	 * @return the current execution time.
	 */
	long getTime() {
		return System.currentTimeMillis();
	}


	/**
	 * Schedules an event for a particular algorithm
	 * 
	 * @param node
	 * @param event
	 */
	void scheduleEvent(AlgorithmNode node,Event event) {
		EventTask task = new EventTask(node,event);

		scheduler.schedule(task,event.getTime());
		events.put(event.getId(),task);
	}

	/**
	 * Cancels the execution of a scheduled event
	 * 
	 * @param id
	 */
	public void cancelEvent(String id){
		EventTask task = events.remove(id);
		if(task != null){
			task.cancel();
		}		
	}


	/**
	 * Returns a proxy to access an algorithm from one application or another Algorithm
	 * using one or more given interfaces
	 * 
	 * @param name
	 * @param interfaces
	 * 
	 * @return a proxy for the algorithm
	 */
	public Object getAlgorithm(String name,Class...interfaces) {

		AlgorithmNode node = algorithms.get(name);

		if(node == null){
			throw new IllegalArgumentException("Algorithm not registered: "+name);
		}

		//return node.getAlgorithm();

		//create a proxy for the algorithm
		return java.lang.reflect.Proxy.newProxyInstance(
											this.getClass().getClassLoader(),
											interfaces,
											new AlgorithmProxy(node));
		 

	}


	/**
	 * Registers an algorithm to be executed on this node
	 * 
	 * @param name
	 * @param algorithm
	 * @throws DatException 
	 */
	void registerAlgorithm(String name, Configuration config) throws DatException {
		
		String algClass = config.getString("class");
		
		Algorithm alg;
		try {
			
			AlgorithmNode node= new AlgorithmNode(this,name,config);	
			algorithms.put(name, node);
			installList.add(node);

		} catch (Exception e) {
			throw new DatException("Exception registering algorithm",e);
		}

		
	}


	/**
	 * Convenience method to register and start and algorithm one the node has been started.
	 *  
	 * @param name
	 * @param algorithm
	 * @param parameters
	 * @throws DatException 
	 */
	void installAlgoritm(String name,Configuration config) throws DatException{
		
		registerAlgorithm(name,config);
		startAlgorithm(algorithms.get(name));
	}
	
	/**
	 * Initiate the execution of this node
	 */
	public void run(){

		//Initialize the node;

		//get failure delay, default is 0. Convert to milliseconds
		failureDelay = configuration.getLong("failure.delay",0)*1000;
		recoveryDelay = configuration.getLong("failure.recovery",0)*1000;


		this.threads = new ThreadGroup(Thread.currentThread().getThreadGroup(),"");

		//Set this instance as the local network node for this thread.
		DAT.setNetworkNode(this);
		
		start();

		if(failureDelay != 0){

			while(true){

				try {
					Thread.sleep(Exponential.nextLong(1.0/failureDelay));

					stop();
					Thread.sleep(Exponential.nextLong(1.0/recoveryDelay));

					start();


				} catch (InterruptedException e) {
					log.debug("interrupted while waiting" + FormattingUtils.getStackTrace(e));
				}		
			}


		}



	}

	/**
	 * Returns the id associated with this Node
	 * TODO: current implementation uses the node location 
	 * @return
	 */
	public UUID getID(){
		return getAddress().getID();
	}

	/**
	 * Schedules an application event after the given delay, in milliseconds
	 * 
	 * TODO: this function is not currently working as the event is executed in the
	 * Timer's thread and not the application thread and therefore the execution context
	 * is not properly set. 
	 * 
	 * @param delay
	 * @param task
	 */
	void schedule(long delay,Runnable task){
		throw new UnsupportedOperationException();

		//scheduler.schedule(new ApplicationTask(task), delay);
	}

	/**
	 * Dispatches a message received from a transport
	 */
	@Override
	public void dispatchMessage(Message message){
		AlgorithmNode algorithm =algorithms.get(message.getAlgorithm());

		log.trace("Dispatching message " + message.toString());

		if(algorithm != null){
			algorithm.signalMessage(message);
		}
		else{
			log.warn("Receiving message for unregistered node" + message.toString());
		}

	}



	/**
	 * Simulates a node failure.Stops the communications and resets the algorithms and 
	 * the application
	 */
	public void stop(){
		try {

			log.info("Stopping node");
			
			//prevent further events and reset timer (creating a new one)
			this.scheduler.cancel();
			this.scheduler = new Timer();

			//stop all algorithm threads
			threads.interrupt();
			//			Thread[] threadList = new Thread[Thread.activeCount()];
			//			threads.enumerate(threadList);
			//			for(Thread t: threadList){
			//				try {
			//					t.join();
			//				} catch (InterruptedException e) {return;};
			//			}


			//disconnect from network
			transport.disconnect();
		} catch (DatException e) {
			log.error("Exception stopping node", e);
		}	

	}


	/**
	 * (re)start the node
	 */
	public void start() {

		try {

			//connect to network
			transport.setHandler(this);
			transport.connect();

			//set debugging context (inherited by sub threads)
			MDC.put("location", transport.getAddress().getLocation());
			//all elements must be initialized, otherwise, a null value is returned
			MDC.put("context","dat");

			log.info("Starting node");
			
			//start the execution of the algorithm in another thread.

			for(AlgorithmNode a: installList){

				startAlgorithm(a);
			}
			
			
			//start application
			new Thread(threads,new AppThread(application)).start();

		} catch (DatException e) {
			log.error("Exception initializing nework node",e);
		} 


	}

	private  void startAlgorithm(AlgorithmNode algorithm){
		String threadName = getAddress().getLocation() + "." + algorithm.getName();
		Thread algorithmThread = new Thread(threads,algorithm,threadName);
		algorithmThread.start();
		
		synchronized(algorithm.getAlgorithm()) {
		try {
			algorithm.getAlgorithm().wait();
		} catch (InterruptedException e) {
			log.error("Exception waiting initialization of algorithm " + algorithm.getName(),e);
		}
		}
	}


	/**
	 * Sends a message over the network
	 * @param destination
	 * @param message
	 * @throws DatException
	 */
	void sendNetworkMessage(NodeAddress destination, Message message) throws DatException {
		try {
			transport.sendNetworkMessage(destination, message);
		} catch (TransportException e) {
			log.trace("Error sending message" + message.toString(), e);
			throw new DatException("Exception sending message to "+ destination.getLocation(),e);
		}
	}


	/**
	 * 
	 * @return returns the address of the node under the network transpot protocol
	 */
	NodeAddress getAddress() {
		return transport.getAddress();
	}

	
	NodeAddress resolve(String address) throws TransportException{
		return transport.resolve(address);
	}



	@Override
	public void handleException(Throwable e) {
		log.error("Unexcepted exception in transport", e);
		
	}

}
