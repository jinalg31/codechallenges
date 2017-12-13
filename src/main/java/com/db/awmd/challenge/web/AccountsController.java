package com.db.awmd.challenge.web;

import java.math.BigDecimal;
import java.util.concurrent.Semaphore;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InSufficientBalanceException;
import com.db.awmd.challenge.exception.NoSuchAccountException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private final AccountsService accountsService;	
	
	private final NotificationService notificationService;
	
	/**
	 * Semaphore instance at class level which ensures only one request executes transfer at a time even if each request is handled independently by the controller class. 
	 * 
	 */
	private static Semaphore lock = new Semaphore(1);

	@Autowired
	public AccountsController(AccountsService accountsService, NotificationService notificationService) {
		this.accountsService = accountsService;
		this.notificationService = notificationService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);

		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(path = "/{accountId}")
	public Account getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		return this.accountsService.getAccount(accountId);
	}

	/**
	 * This service transfer the amount from the given source account to destination account. This is atomic operation which should happen either all or none.
	 * Also, to make the transfer thread-safe, a locking mechanism is used which ensures only one request executes the transfer at a time for parallel executing requests. 
	 * 
	 * @param accountFromId
	 * @param accountToId
	 * @param amount
	 * @return
	 */
	@PostMapping(path = "/transfer/{accountFromId}/{accountToId}/{amount}")
	public ResponseEntity<Object> transferAmount(@PathVariable String accountFromId, @PathVariable String accountToId,
			@PathVariable BigDecimal amount) {		
		
		if(amount.compareTo(new BigDecimal("0")) <= 0){
			return new ResponseEntity<>("Amount can't be in negative!", HttpStatus.BAD_REQUEST);
		}
		
		/* This block of code should be executed all at once (either all or none) hence we need to synchronize this. */
		try {
			lock.acquire();
		} catch (InterruptedException e1) {
			log.debug(e1.getMessage());
			lock.release();
			return new ResponseEntity<>("Request to transfer amount couldn't be completed!", HttpStatus.BAD_REQUEST);
		}
		
		log.info("Transferring amount from : "+accountFromId + " to "+accountToId + " :"+amount);
		
		
		try {
			Account fromAccount = accountsService.accountExists(accountFromId);
			Account toAccount = accountsService.accountExists(accountToId);
			
			// debit from source account
			accountsService.debitAccount(fromAccount, amount);
			// credit to destination account
			accountsService.creditAccount(toAccount, amount);
			
			//Notify accounts on successful transfer
			notificationService.notifyAboutTransfer(fromAccount, "Your account: "+accountFromId + " has been debited by " + amount +". Your account balance is:"+fromAccount.getBalance());
			notificationService.notifyAboutTransfer(toAccount, "Your account: "+accountToId + " has been credited by " + amount +". Your account balance is:"+toAccount.getBalance());
			
		} catch (NoSuchAccountException | InSufficientBalanceException e) {
			log.debug(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		finally{
			lock.release();
		}
		log.info("Completed transfer.");
		
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
