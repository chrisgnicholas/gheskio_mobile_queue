package org.gheskio.queue;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class SimpleAuth extends Authenticator {

	public SimpleAuth() {
		// TODO Auto-generated constructor stub
	}
	
	 // Called when password authorization is needed
	protected PasswordAuthentication getPasswordAuthentication() {
	
		String userVal = "GHESKIO";

		String passwdVal = "stop_HIV_now";
		
		return new PasswordAuthentication(userVal, passwdVal.toCharArray());
	}

}
