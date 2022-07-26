package com.zincworks.service;

import static spark.Spark.*;
import static spark.Spark.port;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.zincworks.assessment.AppException;
import com.zincworks.assessment.AppProperties;
import com.zincworks.bank.BankManager;
import com.zincworks.foundation.util.JsonUtil;

import spark.Filter;

public class AppService implements Runnable {

	public Thread serviceThread;
	public static int port;
	private static boolean running;
	
	private BankManager bankManager;
	
	public AppService() {
		serviceThread = new Thread();
		serviceThread.setDaemon(true);
	}
	
	public final AppService init(final int runningPort) {
		port = runningPort;
				
		return this;
	}
	
	private final static class Filters {

	    // Enable GZIP compression
	    public static final Filter addGzipHeader = (req, res) -> {
	        res.header("Content-Encoding", "gzip");
	    };

	}

	public final void execute() throws AppException {
		
		final Gson gson = new Gson();
		
		try {
			
			// initialise app properties
			AppProperties appProperties = new AppProperties();
			
			// initialise bank 
			bankManager = new BankManager(appProperties.get());
			
		} catch (AppException e) {
			
			throw new AppException(e);
			
		}
		
		// initialise spark 
		if(!running) {
			
			// start services on assigned port
		    port(port);
		    running = true;
		}
		
		// common response to user for any errors
		HashMap<String, String> errorResponse = new HashMap<String,String>();
		errorResponse.put("Message", "Sorry, we are unable to process your request at this time.");
		
		// simple respond to running system
        HashMap<String,String> response = new HashMap<String,String>();
        response.put("Message", "The App service is running");
	    get("/status", (req, res) -> response, gson::toJson);
	    
	    	    
        /**
         * Authenticate user's account number with matching pin and return a valid token if successful 
         */
        post("/auth", (req, res) -> {
        	res.type("application/json");
        	
        	String request = req.body().trim();
        	
        	if(JsonUtil.isValidJson(request)) {
        		
        		try {
                
        			String accountNumber = JsonParser.parseString(request).getAsJsonObject().get("accountNumber").getAsString();
        			int pin = JsonParser.parseString(request).getAsJsonObject().get("pin").getAsInt();
        			
        			return gson.toJson(bankManager.authenticatePin(accountNumber, pin));
        			
        		} catch(Exception e) {
        			
        			return gson.toJson(errorResponse);
        			
        		}
        		
        	} else {
        		
        		return gson.toJson(errorResponse);
        		
        	}
        	
        });
        
        /**
         * Retrieve an account balance for given authentication token
         */
        post("/balance", (req, res) -> {
        	res.type("application/json");
        	 
            String request = req.body().trim();
        	
        	if(JsonUtil.isValidJson(request)) {
        		
        		try {
        		
        			String token = JsonParser.parseString(request).getAsJsonObject().get("token").getAsString();
        		
        		    return gson.toJson(bankManager.getAccountDetails(token));
        			
        		} catch(Exception e) {
        			
        			return gson.toJson(errorResponse);
        			
        		}
        	} else {
        		
        		return gson.toJson(errorResponse);
        		
        	}
        	
        });
        
        post("/withdraw", (req, res) -> {
        	res.type("application/json");
        	 
            String request = req.body().trim();
        	
        	if(JsonUtil.isValidJson(request)) {
        		
        		try {
	        		
        			String token = JsonParser.parseString(request).getAsJsonObject().get("token").getAsString();
	        		int amount = JsonParser.parseString(request).getAsJsonObject().get("amount").getAsInt(); 
	        		
	        		return gson.toJson(bankManager.makeWithdrawal(token, amount));
	        		
        		} catch(Exception e) {
        			
        			return gson.toJson(errorResponse);
        			
        		}
        		
        	} else {
        		
        		return gson.toJson(errorResponse);
        		
        	}
        	
        });
	    
    	// handle access control
        after((Filter) (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST");
        });
        
        // handle invalid requested routes
        after("*", (req, res) -> {
            
        	if (res.body() == null) {
        		
        		// do not respond to any invalid requests
        		
        		// TODO: these could be optionally logged
        		
            }
            
        });
        
        // Set up after-filters for GZIP (always last to be called)
        after("*", Filters.addGzipHeader);
	}
	
	/**
	 * Stop Spark
	 */
	public final void stop() {
		
		if(running) {
		    
			serviceThread.interrupt();
		
		    // stop spark ... 
		    spark.Spark.stop();
		    
		    running = false;
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
