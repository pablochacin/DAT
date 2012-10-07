package dat.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Offers methods to facilitate the manipulation of collections
 * 
 * @author Pablo Chacin
 *
 */
public class CollectionUtils {

	
	public static Map<String,String> mapFromString(Map map,String pairs, String separator){
				
		StringTokenizer st= new StringTokenizer(pairs,separator);
		while (st.hasMoreTokens()) {
			String token= st.nextToken();
			int i= token.indexOf('=');
			if (i < 1)
			    throw new IllegalArgumentException("kev/value pair '" + token + "' is illformed"); //$NON-NLS-1$ //$NON-NLS-2$
			String value= token.substring(i+1);
			token= token.substring(0, i);
			map.put(token, value);
		}
		
		return map;
	}
	
	
	public static Map<String,String> mapFromString(String pairs, String separator){
		return mapFromString(new HashMap<String,String>(),pairs,separator);
	}

}
