package com.zincworks.assessment;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Initialise App properties configuration for use
 * 
 * example usage: 
 * 
 *    AppProperties appProperties = new AppProperties();
 * 
 *    String databaseName = appProperties.get().dbName;
 *
 */
public final class AppProperties {

	// expose Config member variables so that they can be referenced externally
	public final static class Config {
		
		public int sparkPort;
		public String dbHost;
		public String dbName;
	    public int dbPort;
		public String dbUser;
		public String dbPassword;
		
		private Config() {}
    }
	
	private static Config c = null;
	
	public AppProperties() throws AppException {
		
		// load the database configuration from the astrolabe.properties file
		try {
			
			final Properties p = new Properties();
			final File f = new File("./");
	    	
			final FileInputStream file = new FileInputStream(f.getCanonicalPath() + File.separator + "conf" + File.separator + "app.properties");
			
			p.load(file);
			
			file.close();

			final Set<String> keys = p.stringPropertyNames();
			final List<String> configs = new ArrayList<String>();
				
			String config = null;
			
			// retrieve all the separate configuration identifiers
		    for (String key : keys) {
		    
		    	config = key.substring(0, key.indexOf("."));
		    	
		    	// add each configuration item
		    	if(!configs.contains(config)) {
		    		configs.add(config);
		    	}
		    	
		    }
		    
		    // populate the configuration object
		    for(String pk : configs) {
		    	
		    	c = new Config();
		        
		    	// TODO: check that the properties file contains required and valid properties 
		    	
		    	if(p.getProperty(pk+".dbport").trim().isEmpty()) {
		    	
		    		c.sparkPort = 4567; // default Spark port 
		    	
		    	} else {
		    		
		    		c.sparkPort = Integer.valueOf(p.getProperty(pk+".sparkport").trim());
		    		
		    	}
		    	
		        c.dbHost = p.getProperty(pk+".dbhost").trim();
		        c.dbName = p.getProperty(pk+".dbname").trim();
		        
		        if(p.getProperty(pk+".dbport").trim().isEmpty()) {
		        	
		        	c.dbPort = 3306; // default MySQL port
		        	
		        } else {
		        	
			        c.dbPort = Integer.valueOf(p.getProperty(pk+".dbport").trim());
			        
		        }
		        
			    c.dbUser = p.getProperty(pk+".dbuser").trim();
			    
			    if(p.getProperty(pk+".dbpassword").isEmpty()) {
			        
			    	c.dbPassword = "";
			    	
			    } else {
			    	
			    	c.dbPassword = p.getProperty(pk+".dbpassword").trim();
			    	
			    }
		    }		    
		    
		} catch(Exception e) {
			
			throw new AppException(e);
			
		}	
	}
	
	/**
	 * Get the App configuration
	 */
	public final Config get() {
		
		return c;
		
	}
}
