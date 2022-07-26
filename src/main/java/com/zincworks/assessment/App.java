package com.zincworks.assessment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.zincworks.foundation.util.ServiceUtil;
import com.zincworks.service.AppService;


/**
 * Main App
 * 
 * Dependencies: 
 * 
 *   Run the conf/DBMods.sql script
 *   Initialise conf/app.properties file 
 *
 */
public final class App {
    
	private static AppService service; // spark
	
    private static final Logger log; // log4j
	
	static {
		
		log = Logger.getLogger(App.class);
		
	}
	
	public static void main(String[] args) {
        
		System.out.println( "App Service" );
		
		
		try {
			
			File propsFile = new File("./");
		    
		    // load logging configuration
			// TODO: check that log properties file exists
		    PropertyConfigurator.configure(propsFile.getCanonicalPath() + File.separator + "conf" + File.separator + "log4j.properties");
		    
			// load App configuration
			AppProperties appProperties = new AppProperties();
			
			// check if port is available to use
			if(!ServiceUtil.isPortAvailable(appProperties.get().sparkPort)) {
			    
				System.out.println("Unable to start the App service on the specified port.");
				
			} else {
			
				// initialise and run Spark on configured port
				service = new AppService().init(appProperties.get().sparkPort);
				
	            service.execute();
	            
			}
            
		} catch (AppException | IOException e) {
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			
			log.error("App encountered the following exception and had to terminate.", e);
			
			// stop the service
			if(service != null) {
				
				service.stop();
				
			}
		}
		
    }
	
}
