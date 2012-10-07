package dat.algorithms.transaction.twofacecommit;

import java.util.List;

import java.util.UUID;


import dat.DatException;
import dat.Node;
import dat.algorithms.leader.LeaderElection;
import dat.algorithms.membership.MembershipAlgorithm;
import dat.algorithms.transaction.ResourceManager;
import dat.algorithms.transaction.TransactionCoordinator;
import dat.core.DAT;
import dat.network.NodeAddress;
import dat.patterns.requestor.RequestMessage;
import dat.patterns.requestor.RequestReply;
import dat.patterns.requestor.ResponseMessage;


public class TwoFaceCommit extends RequestReply implements TransactionCoordinator {


	private class OperationThread implements Runnable{
		
		private OperationRequest request;
		
		OperationThread(OperationRequest request ){
			this.request = request;
		}
		
		
		public void run(){
			Boolean rc = true;

			if(request.getType().equals(LOCK_OPERATION)){

				rc = manager.lock(request.getString("resource"), 
						request.getString("transaction"));					

			}else if(request.getType().equals(APPLY_OPERATION)){


				manager.apply(request.getString("resource"), 
						request.getString("transaction"),
						request.getString("operation"),
						request.getObject("data"));

			}
			else if(request.getType().equals(ABORT_OPERATION)){

				manager.abort(request.getString("resource"), 
						request.getString("transaction"));
			} 
			else if(request.getType().equals(COMMIT_OPERATION)){

				manager.commit(request.getString("resource"), 
						request.getString("transaction"));

			} 		


			sendResponse(request,new OperationResponse(), rc);
		}
	}

	private class TransactionThread implements Runnable {

		TransactionRequest request;

		public TransactionThread(TransactionRequest request) {
			this.request = request;
		}

		public void run() {
			
			boolean rc = executeTransaction(request.getString("resource"),
					request.getString("transaction"),
					request.getString("operation"),
					request.getObject("data"));


			sendResponse(request,new TransactionResponse(),rc);
		}
	}

	protected static String LOCK_OPERATION = "transaction.lock";

	protected static String APPLY_OPERATION = "transaction.apply";

	protected static String ABORT_OPERATION = "transaction.abort";


	protected static String COMMIT_OPERATION = "transaction.commit";


	protected static int TRANSACTION_TIME_OUT = 5000;

	protected static int REQUEST_TIME_OUT = 1000;

	/**
	 * Manager of resources
	 */
	private ResourceManager manager;

	/**
	 * Algorithm used to elect a leader
	 */
	private LeaderElection election;
	
	/**
	 * Algorithm to discover participants in the transactions
	 */
	private MembershipAlgorithm membership;


	protected long timeout;

	@Override
	public void init(Node node) {
		this.node = node;
		
		membership = (MembershipAlgorithm) node.getAlgorithm(node.getParameters().getString("membership"), 
                MembershipAlgorithm.class);
			
		election = (LeaderElection)node.getAlgorithm(node.getParameters().getString("election"),
				LeaderElection.class);

		timeout = node.getParameters().getLong("timeout");
		election.electLeader();

	}


	@Override
	public boolean executeTransaction(String resource, String operation,Object data){
		return executeTransaction(resource,UUID.randomUUID().toString(),operation,data);
	}


	@Override
	public boolean executeTransaction(String resource, String transaction, String operation, Object data) {


		//If not the leader, delegate to current leader
		if(!election.isLeader()){
			TransactionRequest request = new TransactionRequest();
			request.setString("resource",resource);
			request.setString("transaction",transaction);
			request.setString("operation",operation);
			request.setObject("data",data);

			NodeAddress coordinator = election.getLeader();
			try {
				List<TransactionResponse> responses = (List<TransactionResponse>)makeRequest(request,1,timeout,election.electLeader());

				if(responses.size() == 0){
					node.getLog().debug("No response received from coordinator");
					return false;
				}
				return responses.get(0).getBoolean("rc");

			} catch (DatException e) {
				node.getLog().debug("Exception sending transaction request to coordinator ["+ 
						             coordinator.getLocation() +"]" + e.getMessage());
				NodeAddress newCoordinator= election.electLeader();
				if(!newCoordinator.equals(coordinator)){
					return executeTransaction(resource,transaction,operation,data);
				}
				else{
					node.getLog().debug("Aborting transaction. Can't contact coordinator",e);
				}
			}
		}

		//coordinate update
		boolean locked = manager.lock(resource, transaction) && lockRequest(resource,transaction);

		if(!locked){
			manager.abort(resource, transaction);
			abortRequest(resource,transaction);	
			return false;
		}

		boolean applied = manager.apply(resource, transaction, operation, data) && 
		applyRequest(resource, transaction, operation, data);

		if(!applied){
			manager.abort(resource, transaction);
			abortRequest(resource,transaction);
		}			

		manager.commit(resource, transaction);
		commitRequest(resource,transaction);

		return true;
	}


	public boolean lockRequest(String resource,String transaction){
		OperationRequest lockRequest = new OperationRequest();		
		lockRequest.setType(LOCK_OPERATION);
		lockRequest.setString("resource",resource);
		lockRequest.setString("transaction",transaction);

		boolean locked = requestOperation(lockRequest);

		return locked;
	}


	public boolean applyRequest(String resource, String transaction,String operation,Object data){
		OperationRequest opRequest = new OperationRequest();	
		opRequest.setType(APPLY_OPERATION);
		opRequest.setString("resource",resource);
		opRequest.setString("transaction",transaction);
		opRequest.setString("operation",operation);
		opRequest.setObject("data",data);


		boolean applied = requestOperation(opRequest);

		return applied;
	}


	public boolean abortRequest(String resource,String transaction){
		//if any member rejected the lock
		OperationRequest obortRequest = new OperationRequest();	
		obortRequest.setType(ABORT_OPERATION);
		obortRequest.setString("resource",resource);
		obortRequest.setString("transaction",transaction);
		requestOperation(obortRequest);

		boolean aborted = requestOperation(obortRequest);
		return aborted;
	}


	public boolean commitRequest(String resource,String transaction){

		OperationRequest commitRequest = new OperationRequest();	
		commitRequest.setType(COMMIT_OPERATION);
		commitRequest.setString("resource",resource);
		commitRequest.setString("transaction",transaction);

		boolean committed = requestOperation(commitRequest); 

		return committed;
	}


	@Override
	public void setResourceManager(ResourceManager manager) {
		this.manager = manager;

	}


	protected void sendResponse(RequestMessage request,ResponseMessage response,boolean rc){

		response.setType(request.getType());
		response.setId(request.getId());
		response.setString("resource",request.getString("resource"));
		response.setString("transaction",request.getString("transaction"));
		//copy operation, if any
		response.setString("operation",request.getString("operation"));
		response.setBoolean("rc", rc);

		try {
			DAT.getNode().sendMessage(request.getRequestor(), response);
		} catch (DatException e) {
			DAT.getNode().getLog().warn("Unable to contant requestor "+ request.getRequestor().toString());
		}
	}






	/**
	 * Handle the request for a the execution of a transaction
	 * Should be received only by the leader. 
	 * 
	 * @param request
	 */
	public void processRequest(TransactionRequest request){


		//if not the leader, redirect
		if(!election.isLeader()){
			DAT.getNode().getLog().debug("Receiving a transaction request when not the leader");

			NodeAddress leader = (NodeAddress) election.electLeader();

			try {
				DAT.getNode().sendMessage(leader, request);
			} catch (DatException e) {
				DAT.getNode().getLog().warn("Unable to contact leader");
			}

			return;
		}


		new Thread(new TransactionThread(request)).start();
	}




	/**
	 * Process request over a resource. 
	 *  
	 * @param request
	 */
	public void processRequest(OperationRequest request){
		
		new Thread(new OperationThread(request)).start();

	}



	/**
	 * Sends a requests and processes the response
	 * 
	 * @param request
	 * @return
	 */
	private boolean requestOperation(RequestMessage request){

		List<ResponseMessage> results;
		try {
			results = (List<ResponseMessage>) makeRequest(request, 0, timeout,membership.getKnownNodes());
			
			if((membership.getKnownNodes().size() > 0) && (results.isEmpty())) {
				return false;
			}
			
			boolean rc = true;
			for(ResponseMessage r: results){
				if(!r.getBoolean("rc")) {
					rc = false;
					break;
				}
			}

			return rc;
		} catch (DatException e) {
			DAT.getNode().getLog().debug("Exception sending request",e);
			return false;
		}



	}




}
