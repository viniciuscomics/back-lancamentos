package br.com.example.sistema.utils;

import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class Utils {

	private static Logger log = LoggerFactory.getLogger(Utils.class);
	
	public static void executeFunctionGet(Class<?> clazz, Object instanceClass) {
		try {

			for (Method func : clazz.getMethods()) {

				if (func.getName().startsWith("get") && !func.getName().equals("getClass")) {

					Object result = func.invoke(instanceClass);
					String nameAtribute = func.getName().replace("get", "");
					
					Class<?> classReference = getClassInObjectReference(result);

					if (classReference != null) {					
						
						log.info("Imprimindo o atributos da classe-> " + nameAtribute);
						executeFunctionGet(classReference, result);

					} else if(result != null){
						log.info(MessageFormat.format("{0} = {1}", nameAtribute, result));
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	private static Class<?> getClassInObjectReference(Object reference) {
				
		try {
		if(reference != null && reference.toString().contains("@")) {
			
			int idx = reference.toString().indexOf("@");		
			
			return Class.forName(reference.toString().substring(0,idx));			
		}
		
		return null;
		}
		catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

}
