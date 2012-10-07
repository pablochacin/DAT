package dat.utils;

import java.lang.reflect.Method;

import dat.DatException;

public class ReflectionUtils {

	
	public static Object invoke(String methodName, Object target, Object arg) throws DatException{
		return invoke(methodName,target,arg,Object.class);
	}

	
	public static Object invoke(String methodName, Object target, Object arg,Class superclass) throws DatException{
		
		try{
			Method method = getMethod(methodName, target.getClass(), arg.getClass(), superclass);
			return method.invoke(target, arg);
		}
		catch (Exception e) {
			throw new DatException("Exception invoking method " + methodName + " in class " + target.getClass().getName(),e);
		} 
		
		
	}
	
	/**
	 * Finds a method that matches the name and signature of arguments
	 * 
	 * @param methodName
	 * @param target
	 * @param param
	 * @return
	 * @throws NoSuchMethodException
	 */
	private static Method getMethod(String methodName, Class target, Class arg,Class superclass) throws NoSuchMethodException {
		try {
						
			return target.getMethod(methodName, new Class[]{ arg}); 
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			if(arg != superclass){
				return getMethod(methodName, target, arg.getSuperclass(),superclass);
			}
			else throw e;
		}
		return null;
	}
}
