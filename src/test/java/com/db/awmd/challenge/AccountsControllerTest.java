package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@MockBean
	private EmailNotificationService notificationService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	public void testMockCreation() {
		assertNotNull(notificationService);
	}

	@Test
	public void createAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-123");
		assertThat(account.getAccountId()).isEqualTo("Id-123");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}

	@Test
	public void createDuplicateAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"balance\":1000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoBalance() throws Exception {
		this.mockMvc.perform(
				post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":\"Id-123\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountEmptyAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId)).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
	}

	@Test
	public void transferAmount() throws Exception {
		String accountFromId = "A001";
		Account fromAccount = new Account(accountFromId, new BigDecimal(1000));
		accountsService.createAccount(fromAccount);

		String accountToId = "A002";
		Account toAccount = new Account(accountToId, new BigDecimal(250));
		accountsService.createAccount(toAccount);

		Mockito.doNothing().when(notificationService).notifyAboutTransfer(anyObject(), anyString());

		this.mockMvc.perform(post("/v1/accounts/transfer/A001/A002/300")).andExpect(status().isOk());
		assertThat(fromAccount.getBalance()).isEqualByComparingTo("700");
		assertThat(toAccount.getBalance()).isEqualByComparingTo("550");

	}
	
	@Test
	public void transferAmountInsufficientBalance() throws Exception {
		String accountFromId = "A001";
		Account fromAccount = new Account(accountFromId, new BigDecimal(1000));
		accountsService.createAccount(fromAccount);

		String accountToId = "A002";
		Account toAccount = new Account(accountToId, new BigDecimal(250));
		accountsService.createAccount(toAccount);

		Mockito.doNothing().when(notificationService).notifyAboutTransfer(anyObject(), anyString());

		this.mockMvc.perform(post("/v1/accounts/transfer/A001/A002/1300")).andExpect(status().isBadRequest());
		assertThat(fromAccount.getBalance()).isEqualByComparingTo("1000");
		assertThat(toAccount.getBalance()).isEqualByComparingTo("250");

	}

	@Test
	public void transferNegativeAmount() throws Exception {
		String accountFromId = "A001";
		Account fromAccount = new Account(accountFromId, new BigDecimal(1000));
		accountsService.createAccount(fromAccount);

		String accountToId = "A002";
		Account toAccount = new Account(accountToId, new BigDecimal(250));
		accountsService.createAccount(toAccount);

		Mockito.doNothing().when(notificationService).notifyAboutTransfer(anyObject(), anyString());

		this.mockMvc.perform(post("/v1/accounts/transfer/A001/A002/-300")).andExpect(status().isBadRequest());
		assertThat(fromAccount.getBalance()).isEqualByComparingTo("1000");
		assertThat(toAccount.getBalance()).isEqualByComparingTo("250");

	}

	@Test
	public void transferAmountFromInvalidAccount() throws Exception {
		
		String accountFromId = "A001";
		Account fromAccount = new Account(accountFromId, new BigDecimal(1000));
		accountsService.createAccount(fromAccount);

		String accountToId = "A002";
		Account toAccount = new Account(accountToId, new BigDecimal(250));
		accountsService.createAccount(toAccount);

		Mockito.doNothing().when(notificationService).notifyAboutTransfer(anyObject(), anyString());

		this.mockMvc.perform(post("/v1/accounts/transfer/A00111/A002/300")).andExpect(status().isBadRequest());
		assertThat(fromAccount.getBalance()).isEqualByComparingTo("1000");
		assertThat(toAccount.getBalance()).isEqualByComparingTo("250");

	}

	@Test
	public void transferAmountToInvalidAccount() throws Exception {
		
		String accountFromId = "A001";
		Account fromAccount = new Account(accountFromId, new BigDecimal(1000));
		accountsService.createAccount(fromAccount);

		String accountToId = "A002";
		Account toAccount = new Account(accountToId, new BigDecimal(250));
		accountsService.createAccount(toAccount);

		Mockito.doNothing().when(notificationService).notifyAboutTransfer(anyObject(), anyString());

		this.mockMvc.perform(post("/v1/accounts/transfer/A001/A00222/300")).andExpect(status().isBadRequest());
		assertThat(fromAccount.getBalance()).isEqualByComparingTo("1000");
		assertThat(toAccount.getBalance()).isEqualByComparingTo("250");

	}

	@Test
	public void transferAmountMultipleRequests() throws Exception {
		
		String accountFromId = "A001";
		Account fromAccount = new Account(accountFromId, new BigDecimal(1000));
		accountsService.createAccount(fromAccount);

		String accountToId = "A002";
		Account toAccount = new Account(accountToId, new BigDecimal(550));
		accountsService.createAccount(toAccount);

		Mockito.doNothing().when(notificationService).notifyAboutTransfer(anyObject(), anyString());

		Runnable r1 = () -> {
			try {
				mockMvc.perform(post("/v1/accounts/transfer/A001/A002/400")).andExpect(status().isOk());
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		Runnable r2 = () -> {
			try {
				mockMvc.perform(post("/v1/accounts/transfer/A001/A002/300")).andExpect(status().isOk());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		Runnable r3 = () -> {
			try {
				mockMvc.perform(post("/v1/accounts/transfer/A002/A001/300")).andExpect(status().isOk());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		new Thread(r1).start();
		new Thread(r2).start();
		new Thread(r3).start();

		
		Thread.sleep(15000);
		 assertThat(fromAccount.getBalance()).isEqualByComparingTo("600");
		 assertThat(toAccount.getBalance()).isEqualByComparingTo("950");

	}
}
