package com.db.awmd.challenge.exception;

/**
 * This exception class is used when the source account doesn't have sufficient balance and a debit is attempted.
 * 
 * 
 * @author Jinal.Gohel
 *
 */
public class InSufficientBalanceException extends Exception {
	
	public InSufficientBalanceException(String message){
		super(message);
	}

}
