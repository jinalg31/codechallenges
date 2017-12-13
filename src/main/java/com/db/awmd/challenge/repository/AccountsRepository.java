package com.db.awmd.challenge.repository;

import java.math.BigDecimal;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InSufficientBalanceException;

public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountIdException;

  Account getAccount(String accountId);

  void clearAccounts();
  
  /**
   * This method debits the given amount from the specified source account. It updates the data store
   * with the changes.
   * 
   * @param account
   * @param amount
   * @throws InSufficientBalanceException
   */
  void debitAccount(Account account, BigDecimal amount) throws InSufficientBalanceException;
  
  /**
   * This method credits the the specified account with the given amount and updates the data store.
   * 
   * @param account
   * @param amount
   */
  void creditAccount(Account account, BigDecimal amount) ;
  
}
