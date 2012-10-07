package dat.algorithms.transaction.test;



import java.util.UUID;

import com.sun.org.apache.xml.internal.serializer.utils.Utils;

import dat.algorithms.transaction.ResourceManager;
import dat.algorithms.transaction.TransactionCoordinator;
import dat.core.DAT;
import dat.utils.Exponential;
import dat.utils.FormattingUtils;


public class TransactionApplication implements Runnable, ResourceManager {

	private static String OPERATION = "OPERATION";
	
	private static String DATA = "DATA";
	
	@Override
	public void run() {
		
		
		TransactionCoordinator coordinator = (TransactionCoordinator)DAT.getAlgorithm("transaction", TransactionCoordinator.class);
		coordinator.setResourceManager(this);
		
		double delay = DAT.getAppParameters().getDouble("delay");
		
		try {
			String resource = UUID.randomUUID().toString();
			
			while(true){
				Thread.sleep(Exponential.nextInt(1.0/delay)*1000);
				String transaction = UUID.randomUUID().toString();		
				DAT.getLog().info("Executing transaction " + transaction + 
						          " on resource " + resource);
				String result;
				if(coordinator.executeTransaction(resource, transaction, OPERATION,DATA)){ 
					result = " sucessfull";
				}
				else{
					result = " failed";
				}
				DAT.getLog().info("Transaction " + transaction + 
				          " on resource " + resource + result);

			}
		} catch (InterruptedException e) {
			return;
		}
		
		

	}

	@Override
	public void abort(String resource, String transaction) {
		DAT.getLog().info("Processing Abort request. Resource: " + resource  + 
				          " Transaction: "+transaction);
		
	}

	@Override
	public void commit(String resource, String transaction) {
		DAT.getLog().info("Processing Commit request. Resource: " + resource  + 
		          " Transaction: "+transaction);
		
	}

	@Override
	public boolean apply(String resource, String transaction,
			String operation, Object data) {
		
		DAT.getLog().info("Processing apply request. Resource: " + resource  + 
		          " Transaction: "+transaction + " Operation: " + operation + 
		          " Data: " + (String)(data));
		
		return true;
	}

	@Override
	public boolean lock(String resource, String transaction) {
		DAT.getLog().info("Processing Lock request. Resource: " + resource  + 
		          " Transaction: "+transaction);
		return true;
	}



}
