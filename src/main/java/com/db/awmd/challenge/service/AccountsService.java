package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.NoSuchAccountException;
import com.db.awmd.challenge.exception.InSufficientBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/**
	 * This method debits the specified amount from the given account.
	 * 
	 * @param account Account which is to be debited
	 * @param amount Amount to be debited.
	 * @throws InSufficientBalanceException
	 */
	public void debitAccount(Account account, BigDecimal amount)
			throws InSufficientBalanceException {
		accountsRepository.debitAccount(account, amount);
	}

	/**
	 * This method credits the specified account by the given amount.
	 * 
	 * @param account
	 * @param amount
	 */
	public void creditAccount(Account account, BigDecimal amount)  {
		accountsRepository.creditAccount(account, amount);
	}
	
	/**
	 * This method checks if the account exists in the data store. If not, it throws NoSuchAccountException exception.
	 * 
	 * @param accountId
	 * @return
	 * @throws NoSuchAccountException
	 */
	public Account accountExists(String accountId) throws NoSuchAccountException{
		Account account = accountsRepository.getAccount(accountId);
		// Check if such account exists
		if(account == null){
			throw new NoSuchAccountException("Account "+accountId + " couldn't be found!");
		}
		return account;
	}
}
