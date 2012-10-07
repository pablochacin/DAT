package dat.utils;

import java.util.Random;

/**
 * Generates pseudo random numbers following an Exponential distribution
 * 
 * @author Pablo Chacin
 *
 */
public class Exponential {

	private static Random rand = new Random();
	
	public static double nextDouble(double lambda){
		 
		double value = -(1/lambda)*Math.log(rand.nextDouble());
		
		return value;
	}
	
	
	public static int nextInt(double lambda){
		return(int)Math.round(nextDouble(lambda));
		
	}
	
	
	public static long nextLong(double lambda){
		return Math.round(nextDouble(lambda));
	}
	
	
	public static void main(String[] args){

		
		for(int i= 0;i < 100;i++){
			System.out.println(Exponential.nextLong(1.0/10.0));
		}
	}
}
