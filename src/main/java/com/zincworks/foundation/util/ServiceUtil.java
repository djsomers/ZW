package com.zincworks.foundation.util;

import java.io.IOException;
import java.net.ServerSocket;


public final class ServiceUtil {

	private ServiceUtil() { }
	
	/**
	 * check if a given port is free
	 * @param port integer port number
	 * @return boolean whether port is free or not
	 */
	public final static boolean isPortAvailable(final int port) {
	    
		boolean portFree;
		
	    try (var ignored = new ServerSocket(port)) {
		    portFree = true;
		} catch (IOException e) {
		    portFree = false;
		}
		
		return portFree;
	}
}
