package my.Bank.entities.creator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;
import my.Bank.entities.Account;
import my.Bank.entities.AccountPK;
import my.Bank.jpa.repos.AcctRepo;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)

public class AccountFactoryTest {

	@Autowired
	private AccountFactory accountCreator;
	
	@Autowired
	private AcctRepo acctRepo;	

	@BeforeAll
	public void init() {

	}
	
	@Test
	@Transactional
	public void createAccountEntity() {

		
		String acctId = "123456789012";
		String currency = "USD";
		accountCreator.setAcctId(acctId).setCurrency(currency).buildZeroMoneyEntity();
		
		AccountPK pK = new AccountPK.AccountPKBuilder().setAcctId(acctId).setCurrency(currency).build();
		Optional<Account> accountOpt = acctRepo.findById(pK);

		boolean present = accountOpt.isPresent();
		
		assertEquals(present, true);

		Account account = accountOpt.get();
		Long version = account.getVersion();

		BigDecimal curAmt = account.getCurAmt();
		assertEquals(BigDecimal.ZERO, curAmt);

		assertEquals(0, version);

		account.setCurAmt(new BigDecimal("10.00"));
		acctRepo.saveAndFlush(account);
		version = account.getVersion();		
		assertEquals(1, version);
		
		BigDecimal newAmount = new BigDecimal("88.33");
		account.setCurAmt(newAmount);
		acctRepo.saveAndFlush(account);
		version = account.getVersion();
		assertEquals(2, version);
		
		curAmt = account.getCurAmt();
		assertEquals(newAmount, curAmt);
	}

	@Test
	@Transactional
	public void createAccountNumberTooLong() {

		String setAcctId = "0034567800000";
		String setCurrency = "EUR";
		Account acct = accountCreator.setAcctId(setAcctId).setCurrency(setCurrency).buildZeroMoneyEntity();
		
		assertEquals(null, acct);
	}
	
	@Test
	@Transactional
	public void createAccountCurrencyNotSet() {

		String setAcctId = "034567800000";
		//String setCurrency = "EUR";
		Account acct = accountCreator.setAcctId(setAcctId).buildEntity();
		
		assertEquals(null, acct);
	}	

	@Test
	@Transactional
	public void createWrongCurrencyLength() {

		String setAcctId = "034567800000 ";
		String setCurrency = "EU";
		Account acct = accountCreator.setAcctId(setAcctId).setCurrency(setCurrency).buildEntity();
		
		assertEquals(null, acct);
	}

	@Test
	@Transactional
	public void createAccountEntityCheckCurrency() {
	
		String acctId = "123456780000";
		String setCurrency = "EUR";
		accountCreator.setAcctId(acctId).setCurrency(setCurrency).buildZeroMoneyEntity();
		
		AccountPK pK = new AccountPK.AccountPKBuilder().setAcctId(acctId).setCurrency(setCurrency).build();
		Optional<Account> accountOpt = acctRepo.findById(pK);

		boolean present = accountOpt.isPresent();
		
		assertEquals(present, true);

		Account account = accountOpt.get();
		String currentCurrency = account.getId().getCurrency();
		assertEquals(setCurrency, currentCurrency);
	}
	
	@Test
	@Transactional
	public void createAccountCheckAccountNumber() {
	
		String setAcctId = "003456780000";
		String setCurrency = "EUR";
		accountCreator.setAcctId(setAcctId).setCurrency(setCurrency).buildZeroMoneyEntity();
		
		AccountPK pK = new AccountPK.AccountPKBuilder().setAcctId(setAcctId).setCurrency(setCurrency).build();

		Optional<Account> accountOpt = acctRepo.findById(pK);
		boolean present = accountOpt.isPresent();
		assertEquals(present, true);

		Account account = accountOpt.get();
		String acctId = account.getId().getAcctId();
		assertEquals(setAcctId, acctId);
	}
	
	@Test
	@Transactional
	public void createWithThreeTheSamePk_ZeroMoney() {
	
		String setAcctId = "003456780009";
		String setCurrency = "EUR";
		Account accOne = accountCreator.setAcctId(setAcctId).setCurrency(setCurrency).buildZeroMoneyEntity();
		
		AccountPK pK = new AccountPK.AccountPKBuilder().setAcctId(setAcctId).setCurrency(setCurrency).build();
		Optional<Account> accountOpt = acctRepo.findById(pK);
		assertEquals(accountOpt.isPresent(), true);

		
		
		Account accTwo = accountCreator.setAcctId(setAcctId).setCurrency(setCurrency).buildZeroMoneyEntity();
		assertEquals(accTwo, null);
		

		Account accThree = accountCreator.setAcctId(setAcctId).setCurrency(setCurrency).buildZeroMoneyEntity();
		assertEquals(accThree, null);
		
		
		assertNotNull(accOne, "Account One NULL !");
	}
	
	@Test
	@Transactional
	public void create_pk_ThreeTimesTheSame() {
	
		String setAcctId = "003456780009";
		String setCurrency = "EUR";
		BigDecimal amount = new BigDecimal("88.33");
		
		Account accOne = accountCreator.setAcctId(setAcctId).setCurrency(setCurrency).setCurAmt(amount).buildEntity();
		
		AccountPK pK = new AccountPK.AccountPKBuilder().setAcctId(setAcctId).setCurrency(setCurrency).build();
		Optional<Account> accountOpt = acctRepo.findById(pK);
		assertEquals(accountOpt.isPresent(), true);

		
		
		Account accTwo = accountCreator.setAcctId(setAcctId).setCurrency(setCurrency).setCurAmt(amount).buildEntity();
		assertEquals(accTwo, null);
		
		
		Account accThree = accountCreator.setAcctId(setAcctId).setCurrency(setCurrency).setCurAmt(amount).buildEntity();
		assertEquals(accThree, null);
		
		
		assertNotNull(accOne, "Account One NULL !");
	}	
	
}
