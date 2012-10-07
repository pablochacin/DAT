package dat.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.log4j.MDC;


/**
 * 
 * Mediates in the invocation to the methods exposed by the algorithm to the application 
 * and other algorithms, allowing for tracing, locking, etc. 
 * 
 * This default implementation adds no functionality. 
 * 
 * @author Pablo Chacin
 *
 */
public class AlgorithmProxy implements InvocationHandler {

	private AlgorithmNode node;
	
	public AlgorithmProxy(AlgorithmNode node){
		this.node = node;
	}
	
	@Override
	/**
	 * Invokes the method in the target algorithms, ensuring the environment is
	 * properly set.
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	
			//save current state
			AlgorithmNode currentNode = null;
			
			//If call is made from application, there's no current algorithm
			//and a IllegalStateException is thrown;
			try{
				currentNode = DAT.getNode();
			}catch(IllegalStateException e){};
			
			DAT.setAlgorithmNode(node);
			MDC.put("algorithm", node.getName());
			
			Object result =  method.invoke(node.getAlgorithm(), args);
			
			//restore state
			DAT.setAlgorithmNode(currentNode);
			if(currentNode != null) {
				MDC.put("algorithm", currentNode.getName());
			}
			return result;
	}

}
