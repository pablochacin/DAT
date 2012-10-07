package dat.utils;

import java.util.Random;

/**
 * Generates numbers following an exponential distribution.
 * 
 * @author Pablo Chacin
 *
 */
public class Poisson {

	private static Random rand = new Random();
	

	public static int nextInt(Double lambda) {
		int sum = 0;
		int n= -1;
		while(sum < lambda){
			n+= 1;
			sum -= Math.log(rand.nextDouble());
		}
		
		return n;
	}

	
	public static long nextLong(Double lambda){
		return (long)nextInt(lambda);
	}
	
	public static void main(String[] args){

		
		for(int i= 0;i < 100;i++){
			System.out.println(Poisson.nextInt(3.0));
		}
	}
}
