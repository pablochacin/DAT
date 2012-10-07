package dat.core;


import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import dat.DatException;
import dat.network.Network;
import dat.network.NodeAddress;
import dat.utils.CmdLineArgs;
import dat.utils.LoggingUtils;


/**
 * Offers static methods to access the DAT framework, working as a facade. It also serves a main 
 * application, retrieving parameters from command line and launching the NetworkNodes.
 * 
 * Supported command line arguments
 * <ul>
 * <li> -config.file <path>: path to the configuration file. Default is "dat.properties"
 * <li> -config.log <path>: path to the log configuration file. Default is "log4j.properties" 
 * </ul>
 * 
 * @author Pablo Chacin
 *
 */
public class DAT {
	
	private static String DEFAULT_CONFIG = "dat.properties";
	
	private static String DEFAULT_LOG_CONFIG = "log4j.properties";
	

	/**
	 * Network node associated with the current thread.
	 */
	private static InheritableThreadLocal<NetworkNode> networkNode = new InheritableThreadLocal<NetworkNode>();
		
	/**
	 * Sets the currently executing NetworkNode for a thread.
	 * @param node
	 */
	static void setNetworkNode(NetworkNode node){
		networkNode.set(node);
	}	

	
	/**
	 * Returns the {@link NetworkNode} under which the current thread runs.
	 * 
	 * @return the current {@link NetworkNode} 
	 */
	public static NetworkNode getNetworkNode(){
		NetworkNode node = networkNode.get();

		if(node == null){
			throw new IllegalStateException("The DAT framework has not been properly initiated");
		}

		return node;
	}


	/**
	 * Algorithm node associated with the current thread. Valid only for algorithms
	 */
	private static InheritableThreadLocal<AlgorithmNode> algorithmNode = new InheritableThreadLocal<AlgorithmNode>();

	
	public static void setAlgorithmNode(AlgorithmNode node){
		algorithmNode.set(node);
	}
		
	/**
	 * Returns the {@link AlgorithmNode} under which the current thread runs, if any.
	 * This method is valid only when called from a registered algorithm
	 * 
	 * @return the current {@link NetworkNode} 
	 */
	public static AlgorithmNode getNode(){
		
		AlgorithmNode node = algorithmNode.get();

		if(node == null){
			throw new IllegalStateException("This thread is not running as a registered algorithm");
		}

		return node;
	}
	

	/**
	 * Logger for currently executiong thread
	 */
//	protected static ThreadLocal<Logger> log = new ThreadLocal<Logger>();
//	
//	static void setLog(Logger threadLog){
//		log.set(threadLog);
//	}
//	
//	public static Logger getLog(){
//		return log.get();
//	}
	
	private static Logger appLog = Logger.getLogger("dat.app");
	
	public static Logger getLog(){
		return appLog;
	}
	
	public static Object getAlgorithm(String name,Class...interfaces){
		return networkNode.get().getAlgorithm(name, interfaces);
	}
	
	public static void schedule(long delay,Runnable task){
		networkNode.get().schedule(delay,task);
	}

	
	public static Configuration getAppParameters(){
		return networkNode.get().getAppParameters();
	}
	
	/**
	 * Dummy application that does nothing.
	 * 
	 */
	public static class DummyApp implements Runnable{
		public void run(){};
	}
	

	/**
	 * Class used to instantiate 
	 */
	private Class<? extends Network> networkClass;
	
	
	/**
	 * Class used to instantiate application
	 */
	private Class<? extends Runnable> applicationClass; 
	
	
	private Logger log = Logger.getLogger("dat");

	
	/**
	 * Constructor
	 */
	public DAT(){ 
				
	}	

	
	public static NodeAddress getLocalAddress(){
		return networkNode.get().getAddress();
	}
	
	private void start(String[] args){

		
		CompositeConfiguration config = new CompositeConfiguration();
		
		try{
			Configuration arguments = CmdLineArgs.getArguments(args);
			config.addConfiguration(arguments);
			
			String configFile = arguments.getString("config.file",DEFAULT_CONFIG);
			Configuration fileConfig = new PropertiesConfiguration(configFile);
			config.addConfiguration(fileConfig);
						
			
		}catch(IllegalArgumentException e){
			System.err.println("Syntax error in the arguments " + e.getMessage());
			System.exit(1);
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			System.err.println("Error loading configuration file: " + e.getMessage());
			System.exit(1);
		}
		
		
		Configuration datParms = config.subset("dat");
		
		if(datParms.getBoolean("printconfig", false)){
			Iterator<String> props = config.getKeys();
			while(props.hasNext()){
				String p = props.next();
				System.out.println(p + "=" + config.getString(p));
			}
		}
		
		//initialize log
		String logConfig = config.getString("config.log",DEFAULT_LOG_CONFIG);
		
		LoggingUtils.initLogging(logConfig);
		Logger datLog = Logger.getLogger("dat");
		
		//set the log level, using the default specified in the config.log file
		String logLevel = datParms.getString("loglevel",datLog.getLevel().toString());
		Logger.getLogger("dat").setLevel(Level.toLevel(logLevel));
		
		
		int nodes = 0;
		try{
			 nodes= config.getInt("network.nodes",1);
		}catch(NumberFormatException e){
			System.err.println("Invalid format for parameter network.numNodes");
			System.exit(0);
		}
		
		try {
		
			
			//get the network's class
			String networkClassName = config.getString("network.class");

			networkClass = (Class<? extends Network>) Class.forName(networkClassName);
			
			
			//get application. If none specified, use a default dummy applicator
			String applicationClassName = config.getString("app.class",DummyApp.class.getName());
			applicationClass = (Class<Runnable>) Class.forName(applicationClassName);
			
			String appLogLevel = config.getString("app.loglevel");
			if(appLogLevel != null){
				Logger.getLogger("dat.app").setLevel(Level.toLevel(appLogLevel));
			}
			
			
			//get node's configuration parameters
			Configuration nodeConfig = config.subset("node");
			
			Iterator<String> props = nodeConfig.getKeys();
			while(props.hasNext()){
				String p = props.next();
				System.out.println(p + "=" + nodeConfig.getString(p));
			}
			
			//application configuration
			Configuration appConfig = config.subset("app.param");
			
			//network configuraion
			Configuration networkConfig = config.subset("network.param");
			
			//start each node
			for(int n=0;n < nodes;n++){

				Network network = networkClass.newInstance();
				network.init(networkConfig);
				Runnable application = applicationClass.newInstance();
				NetworkNode node = new NetworkNode(nodeConfig,network,application,appConfig);
								
				//install algorithms
				registerAlgorithms(node,config);
				
				//start node in a new Thread
				new Thread(node).start();
			}

		
		} catch (Exception e) {
			log.error("Initiaization exception",e);
			System.exit(0);
		} 
		


	}


	/**
	 * Convenience method to install and start an algorithm from the application.
	 * 
	 * @param name
	 * @param algorithm
	 * @param parameters
	 * @return
	 * @throws DatException 
	 */
	public static void installAlgorithm(String name,Configuration parameters) throws DatException{
		networkNode.get().installAlgoritm(name, parameters);
		
	}


	/**
	 * Convenience method to install an algorithm with a Map of parameters.
	 * 
	 * @param name
	 * @param algorithm
	 * @param parameters
	 * @return
	 * @throws DatException 
	 */
	public static void installAlgorithm(String name,Map parameters) throws DatException{
		installAlgorithm(name,new MapConfiguration(parameters));
	}
	
		
	protected void registerAlgorithms(NetworkNode node,Configuration config) throws DatException {
		
		for(String algName: config.getStringArray("node.algorithms")){
			try {
						
				Configuration algConfig = config.subset("alg."+algName);
				node.registerAlgorithm(algName, algConfig);
			} catch (Exception e) {
				throw new DatException("Unable to register algorithm " + algName,e);
			}

		}
	}
	

	public static void restart(long delay){
		
		NetworkNode node = getNetworkNode();
		
		node.stop();
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			return;
		}

		node.start();
	}
	
	public static void restart(){
		restart(0);
	}
	
	public static void main(String args[]){
		new DAT().start(args);
	}
}
