package dat.patterns.requestor;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import dat.Algorithm;
import dat.DatException;
import dat.Event;
import dat.Message;
import dat.Node;
import dat.algorithms.gcast.GroupcastProtocol;
import dat.network.NodeAddress;
import dat.utils.FormattingUtils;
import dat.utils.ReflectionUtils;

/**
 * Provides the basic functionality to handle requests and wait for multiple responses.
 * 
 * Subclasses must assure that request messages are a subclass of RequestMessage and responses
 * are subclasses or ResponseMessage. They must also provide handler methods for each request
 * subclass:
 * 
 * public void processRequest(MyRequestMessage message)
 * 
 * where MyRequestMessage extends RequestMessage.
 * 
 * @author Pablo Chacin
 *
 */
public abstract class RequestReply implements Algorithm {

	/**
	 * 
	 * Encapsulates all the information about a request
	 */
	class Request {
		
		/**
		 * Id of the request
		 */
		public String id;
		
		/**
		 * Condition used to signal the completation of the request
		 */
		public Condition finished;
		
		public List<ResponseMessage> responses;
		/**
		 * Number of messages expected
		 */
		public int count;
		
		/**
		 * Maximum time to wait for responses
		 */
		public long timeout;

		public Request(String id, int count, long timeout, Condition finished) {
			super();
			this.id = id;
			this.count = count;
			this.timeout = timeout;
			this.finished = finished;
			this.responses = new ArrayList<ResponseMessage>();
		}
		
		
	}
	
	/**
	 * Node on which this algorithm executes
	 */
	protected Node node;
	
	protected GroupcastProtocol gcast;
	
	/**
	 * Keeps track of requests issued by this algorithm instance and the corresponding responses
	 */
	protected Map<String,Request> pendingRequests;
	
	/**
	 * Maintains a register of requests received to avoid processing them multiple times
	 */
	protected Set<String> requestsReceived;
	
	
	protected Lock lock = new ReentrantLock();
	
	public RequestReply(){
		this.pendingRequests = new HashMap<String,Request>();
		this.requestsReceived = new HashSet<String>();

	}
		
	public void init(Node node){
		this.node = node;

	}
	
	
	
	/**
	 * Sends a request to one or more targets and waits for a response. 
	 * 
	 * @param message
	 * @param count
	 * @param timeout
	 * @param targets
	 * @return
	 * @throws DatException
	 */
	protected List<? extends ResponseMessage> makeRequest(RequestMessage message,int count,long timeout,List<NodeAddress> targets) throws DatException{
		
		int responses = count;
		if(count == 0){
			responses = targets.size();
		}
		
		Request request = addRequest(message.getId(),responses,timeout);
	
		sendRequests(message,targets);
		
		return waitResponse(request);
	}

	/**
	 * Convenience method used to send a request to a single target
	 * 
	 * @param message
	 * @param count
	 * @param timeout
	 * @param target
	 * @return
	 * @throws DatException
	 */
	protected List<? extends ResponseMessage> makeRequest(RequestMessage message,int count,long timeout,NodeAddress target) throws DatException{

		return makeRequest(message,count,timeout,Collections.singletonList(target));
	}
	
	/**
	 * Sends the request message to a list of targets.
	 * 
	 * This method can be overridden by subclasses to handle how the messages are sent  
	 * 
	 * @param message
	 * @param targets
	 * @throws DatException
	 */
	protected void sendRequests(RequestMessage message,List<NodeAddress> targets) throws DatException {
		
		for(NodeAddress a: targets){
			node.sendMessage(a, message);
		}
	}
	
	/**
	 * Creates a new requests
	 * @param id
	 * @param count
	 * @param timeout
	 * @return
	 */
	private Request addRequest(String id,int count,long timeout){
		
		if((timeout == 0) && (count == 0)){
			throw new IllegalArgumentException("Count and Timeout can't be both 0");
		}
		
		if(pendingRequests.containsKey(id)){
			throw new IllegalArgumentException("Invalid Id. Request [" + id +"] already exists");
		}
	
		
		//register request
		Request request = new Request(id,count,timeout,lock.newCondition());
		
		pendingRequests.put(id, request);
		
		return request;
	}
		
	/**
	 * Waits for the responses of a request to be received
	 * 
	 * NOTE: this method must be executed under the caller's thread, because it blocks until responses arrive 
	 *       or the timeout expires
	 * 
	 * @param id
	 * @throws DatException
	 */
	private List<? extends ResponseMessage> waitResponse(Request request) throws DatException{
		
			
		if(request.timeout != 0){
			
			node.scheduleEvent(new RequestTimeout(request.id,request.timeout,"request.timeout"));
		}
	
		
    	//wait for responses to finish		
		lock.lock();
		try {
			request.finished.await();
			return request.responses;
			
		} catch (InterruptedException e) {
			throw new DatException("Interrupted while waiting for responses");
		}
		finally{
			lock.unlock();
		}
	}
	
	
	public void handleMessage(Message message){
		node.getLog().warn("Ingoring  message because it's not a Request nor a Response: " + message.toString());
	}
	
	/**
	 * Handle the request
	 * @param message
	 */
	public void handleMessage(RequestMessage message){
		
		dispatchRequest(message);
	}
	

	/**
	 * Handle the responses
	 * @param message
	 */
	public void handleMessage(ResponseMessage message){
		
		dispatchResponse(message);
	}
	
	
	/**
	 * Default request handler, called if the sub-classes don't handle the request.
	 * @param request
	 */
	protected void processRequest(RequestMessage request){
		node.getLog().warn("Ingoring request message" + request.toString());
	}
	
	
	/**
	 * 
	 * @param request
	 */
	protected void dispatchRequest(RequestMessage request){
		
		//if already received, ignore
		if(requestsReceived.contains(request.getId())){
			return;
		}
	
		requestsReceived.add(request.getId());
	
		try {
			ReflectionUtils.invoke("processRequest",this,request);
		} catch (DatException e) {
			node.getLog().warn("Exception handling request type " + request.getClass().getName() + 
				          "\n"+FormattingUtils.getStackTrace(e));
		}
}
	
	/**
	 * Handles the response messages. If still active, 
	 *  
	 * @param message
	 */
	protected void dispatchResponse(ResponseMessage message) {
		Request request = pendingRequests.get(message.getId());
		
		//ignore if the request has finished
		if(request == null){
			return;
		}
		
		request.responses.add(message);
		
		//check if completed
		if((request.count !=0 ) && (request.responses.size() >= request.count)){
			pendingRequests.remove(message.getId());
			lock.lock();
			request.finished.signal();
			lock.unlock();
		}
	}
	
	
	@Override
	public void handleEvent(Event event) {
		node.getLog().warn("Ingoring event " + event.toString());

	
	}
	
	
	public void handleEvent(RequestTimeout event) {
		
		Request request = pendingRequests.get(event.getId());
		if(request == null){
			node.getLog().debug("Ignoring timeout event for request "+ event.getId());
			return;
		}
		
		lock.lock();
		request.finished.signal();
		lock.unlock();

	}
	
}
