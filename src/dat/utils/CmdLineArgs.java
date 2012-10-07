package dat.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

/**
 * Handles command line arguments
 * 
 * @author Pablo Chacin
 *
 */
public class CmdLineArgs {

	/**
	 * Returns a Configuration with the valid arguments taken from the command line.
	 * 
	 * Commands are assumed to be in the form "-k [v]", where "k" is the key and
	 * "v" is an (optional) value. Keys are key sensitive ("-k" is different from "-K")
	 * 
	 * The resulting map has the k,v pairs for parameters. Notice that the trailing "-" is 
	 * removed from parameter's key. 
	 * 
	 * @param argsMap a Map to put the commands. 
	 * @param args arguments from the command line
	 * @param caseSensitve a boolean indicating if keys are key sensitive or not
	 * 
	 * @throws IllegalArgumentException if there is syntax error in the arguments
	 */
	public static Configuration getArguments(String[] args) throws IllegalArgumentException{


		Map<String,String>argsMap = new HashMap<String,String>();
		
		//reads all the arguments and assumes
		for(int i = 0; i < args.length;) {
			String token = args[i];

			//Check that token is a valid command key
			if((token.length() < 2) && (!token.startsWith("-"))){
				throw new IllegalArgumentException("Invalid synthax. Command line option expected: " + token);
			}

			//remove trailing "-"
			String cmd = token.substring(1);

			String value = null;

			if((i<args.length -1) && !args[i+1].startsWith("-")){
				value = args[i+1];
				i++;
			}
			argsMap.put(cmd, value);	
			i++;
		}
		
		Configuration config = new MapConfiguration(argsMap);
		
		return config;
	}



}
