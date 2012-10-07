package dat.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Offers utility functions for formatting outputs of different data types.
 * 
 * 
 * @author Pablo Chacin
 *
 */
public class FormattingUtils {

	/**
	 * Returns the stack trace of a exception as a string.
	 * 
	 * @param cause exception to be processed
	 * @return a string with the exceptin's stack trace
	 */
    public static String getStackTrace(Throwable cause)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        cause.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    /**
     * Returns an array of objects as a String of the format "[value,value,...]"
     * 
     * @param array
     * @return a String with the values of the objects of the array
     */
    public static String arrayToString(Object[] array){
    	
    	if(array == null){
    		return "null";
    	}
    	
    	if(array.length == 0){
    		return "[]";
    	}
    	
    	StringBuffer content = new StringBuffer();
    	content.append("[");
    	content.append(array[0].toString());
    	for(int i=1;i<array.length-i;i++){
    		content.append(array[i].toString());
    		content.append(",");
    	}
		content.append(array[array.length-1].toString());
    	content.append("]");
    	
    	return content.toString();
    }
    
    public static String listToString(List list){
    	if(list == null){
    		return "null";
    	}
    	
    	if(list.size() == 0){
    		return "[]";
    	}
    	
    	StringBuffer content = new StringBuffer();
    	content.append("[");
    	for(Object o: list){
    		content.append("[");
    		content.append(o.toString());
    		content.append("]");
    	}
    	content.append("]");
    	
    	return content.toString();
    }
    
    
    public static String collectionToString(Collection collection){
    	if(collection == null){
    		return "null";
    	}
    	
    	if(collection.size() == 0){
    		return "[]";
    	}
    	
    	StringBuffer content = new StringBuffer();
    	content.append("[");
    	for(Object o: collection){
    		content.append("[");
    		content.append(o.toString());
    		content.append("]");
    	}
    	content.append("]");
    	
    	return content.toString();
    }
    
    public static String mapToString(Map map){
    	if(map == null){
    		return "null";
    	}
    	
    	if(map.size() == 0){
    		return "[]";
    	}
    	
    	StringBuffer content = new StringBuffer();
    	content.append("[");
    	for(Object k: map.keySet()){
    		Object v = map.get(k);
    		if((k == null) || (v == null)){
    			continue;
    		}
    		content.append("[");
    		content.append(k.toString());
    		content.append("=");
    		content.append(v.toString());
    		content.append("]");
    	}
    	content.append("]");
    	
    	return content.toString();
    }
}
