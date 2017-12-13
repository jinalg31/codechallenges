package com.db.awmd.challenge.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InSufficientBalanceException;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public void debitAccount(Account account, BigDecimal amount)
			throws InSufficientBalanceException {		
		
		// Check if it has sufficient balance to perform the transaction
		  if(account.getBalance().compareTo(amount) > 0){
			  account.setBalance(account.getBalance().subtract(amount)); 
		  }else{
			  throw new InSufficientBalanceException("Account: "+account.getAccountId() + " does not have sufficient balance to perform this transaction!");
		  }

	}

	@Override
	public void creditAccount(Account account, BigDecimal amount) {
		account.setBalance(account.getBalance().add(amount)); 
		
	}	

}
