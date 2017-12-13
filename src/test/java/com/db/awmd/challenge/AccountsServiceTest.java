package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InSufficientBalanceException;
import com.db.awmd.challenge.exception.NoSuchAccountException;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}

	}

	@Test
	public void debitAccount() {

		BigDecimal balance = new BigDecimal(1000);
		Account account = new Account("A001234", balance);

		accountsService.createAccount(account);

		try {
			BigDecimal amount = new BigDecimal("300");
			accountsService.debitAccount(account, amount);
			assertThat(account.getBalance()).isEqualByComparingTo(balance.subtract(amount));
		} catch (InSufficientBalanceException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void debitAccountInSufficientBalance() {
		
		BigDecimal balance = new BigDecimal(100);
		Account account = new Account("A0111",balance);
		accountsService.createAccount(account);

		try {
			BigDecimal amount = new BigDecimal(300);
			accountsService.debitAccount(account, amount);

		} catch (InSufficientBalanceException e) {
			assertThat(e.getMessage()).isEqualTo("Account: " + account.getAccountId()
					+ " does not have sufficient balance to perform this transaction!");
		}

	}

	@Test
	public void creditAccount() {
		
		BigDecimal balance = new BigDecimal(1000);
		Account account = new Account("A00123",balance);
		accountsService.createAccount(account);

		BigDecimal amount = new BigDecimal(300);
		accountsService.creditAccount(account, amount);
		assertThat(account.getBalance()).isEqualByComparingTo(balance.add(amount));
	}

	@Test
	public void accountExists() {
		String accountId = "A0011";
		BigDecimal balance = new BigDecimal(1000);
		Account account = new Account(accountId,balance);
		
		accountsService.createAccount(account);

		try {
			Account repoAccount = accountsService.accountExists(accountId);
			assertThat(repoAccount.getAccountId()).isEqualToIgnoringCase(accountId);
		} catch (NoSuchAccountException e) {
			e.printStackTrace();
		} 

	}
	
	@Test
	public void accountExistsNoSuchAccount() {
		String accountId = "A002";
		BigDecimal balance = new BigDecimal(1000);
		Account account = new Account(accountId,balance);
		
		accountsService.createAccount(account);
		String invalidAcctId = "A003";
		try {
			
			accountsService.accountExists(invalidAcctId);
			
		} catch (NoSuchAccountException e) {
			assertThat(e.getMessage()).isEqualTo("Account "+invalidAcctId + " couldn't be found!");
		} 

	}

}
