package dat.algorithms.transaction;


/**
 * Represents an entity that manages a set of resources and controls its 
 * access under transactional operations. 
 *  
 * @author Pablo Chacin
 *
 */
public interface ResourceManager {

	/**
	 * Reserve the resource
	 * 
	 * @param resource String with the identification of the resource
	 * @param transaction a String with the id of the transaction
	 * 
	 * @return a boolean indicating if the resource could be locked (true) or not (false)
	 */
	public boolean lock(String resource,String transaction);
	
	
	/**
	 * Request the tentative execution of an operation over a resource
	 * previously reserved by a lock operation. The operation is identified by a
	 * string which depends of the type of resource.
	 * 
	 * @param resource String with the identification of the resource
	 * @param transaction a String with the id of the transaction
	 * @param operation an String with the id of the operation (dependent of the type of resource)
	 * @param data arguments for the operation
	 * 
	 * @return a boolean indicating if the operation could be executed or not.
	 */
	public boolean apply(String resource,String transaction ,String operation,Object data);
	
	/**
	 * Requests to make permanent the the last tentative operation 
	 *   
	 * @param resource String with the identification of the resource
	 * @param transaction a String with the id of the transaction
	 */
	public void commit(String resource,String transaction); 

	/**
	 * 
	 * @param resource String with the identification of the resource
	 * @param transaction a String with the id of the transaction
	 */
	public void abort(String resource,String transaction);


}
