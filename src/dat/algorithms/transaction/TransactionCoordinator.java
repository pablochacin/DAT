package dat.algorithms.transaction;

public interface TransactionCoordinator {

	/**
	 * Establishes the entity that will manage the transactional access  to a set of resources.
	 * 
	 * @param manager the ResourceManager that control the resources
	 */
	public void setResourceManager(ResourceManager manager);
	
	
	/**
	 * Convenience method. Executes transaction with a id generated by the transaction coordinator.
	 * 
	 * @param recurs
	 * @param operation
	 * @param data
	 * @return
	 */
	public boolean executeTransaction(String resource,String transaction,String operation,Object data);
	
	/**
	 * Convenience method with a transaction coordinator defined transaction id.
	 * 
	 * @param resource
	 * @param operation
	 * @param data
	 * @return
	 */
	public boolean executeTransaction(String resource,String operation,Object data);
		
}
