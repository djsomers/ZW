package com.zincworks.foundation.util;

import com.google.gson.Gson;


/**
 * Static JSON utility functions 
 */
public final class JsonUtil {

	private JsonUtil() {

	}

	/**
	 * Check is a given string is valid JSON
	 * @param json String JSON string to validate
	 * @return boolean whether JSON is valid 
	 */
	public final static boolean isValidJson(final String json) throws UtilException {
		
		final Gson gson = new Gson();
		
		try {
			  
			// attempt to convert string to JSON
			gson.fromJson(json, Object.class);
			
			// successful conversion 
			return true;
	          
	    } catch(com.google.gson.JsonSyntaxException ex) {
	    	
	    	// failed conversion
	        return false;
	        
	    } catch (Exception ex1) {
	    	
	    	// FIXME: Do we need to handle this exception?
	        // throw new UtilException(ex1);
	        
	        return false;
	        
	    }
	}
	
	/*public final static HashMap<String,Object> toHashMap(final String json) {
		HashMap<String, Object> retMap = new Gson().fromJson(
			    json, new com.google.gson.reflect.TypeToken<HashMap<String, Object>>() {}.getType()
			);
		
		return retMap;
	}
	
	public final static HashMap<String,JsonElement> toJsonMap(final String json) {
		HashMap<String, JsonElement> retMap = new Gson().fromJson(
			    json, new com.google.gson.reflect.TypeToken<HashMap<String, JsonElement>>() {}.getType()
			);
		
		return retMap;
	}*/
}