package com.db.awmd.challenge.exception;

/**
 * This exception is thrown when the requested account is not found in data store.
 * 
 * @author Jinal.Gohel
 *
 */
public class NoSuchAccountException extends Exception {

	public NoSuchAccountException(String message){
		super(message);
	}

	
}
