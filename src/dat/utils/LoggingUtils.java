package dat.utils;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



public class LoggingUtils {


	public static void initLogging(String configuration){
		
		PropertyConfigurator.configure(configuration);
	}
	
	public static void setLevel(String loggerName,String levelName){
		
		Logger logger = Logger.getLogger(loggerName);
		
		try {
			Level level = (Level)Level.class.getField(levelName.toUpperCase()).get(logger);
			logger.setLevel(level);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid Logging level: "+ levelName);
		} 
		
	}

	private static Appender ConsoleAppender() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
