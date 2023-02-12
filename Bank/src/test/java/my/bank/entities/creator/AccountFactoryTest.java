package my.bank.entities.creator;

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

import my.bank.entities.Account;
import my.bank.entities.AccountDto;
import my.bank.entities.AccountPK;
import my.bank.entities.creator.AccountFactory;
import my.bank.jpa.repos.AcctRepo;


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
		
		AccountDto acctDto = new AccountDto.AccountDtoBuilder().setAcctId(acctId).setCurrency(currency).buildDto();
		accountCreator.newEntity(acctDto);
		
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

		AccountDto acctDto = new AccountDto.AccountDtoBuilder().setAcctId(setAcctId).setCurrency(setCurrency).buildDto();
		Account account = accountCreator.newEntity(acctDto);

		assertEquals(null, account);
	}
	
	@Test
	@Transactional
	public void createAccountCurrencyNotSet() {
		
		String setAcctId = "034567800000";
		//String setCurrency = "EUR";
		AccountDto acctDto = new AccountDto.AccountDtoBuilder().setAcctId(setAcctId).buildDto();
		Account account = accountCreator.newEntity(acctDto);

		assertEquals(null, account);
	}	

	@Test
	@Transactional
	public void createWrongCurrencyLength() {

		String setAcctId = "034567800000 ";
		String setCurrency = "EU";
		
		AccountDto acctDto = new AccountDto.AccountDtoBuilder().setAcctId(setAcctId).setCurrency(setCurrency).buildDto();
		Account account = accountCreator.newEntity(acctDto);
		
		assertEquals(null, account);
	}

	@Test
	@Transactional
	public void createAccountEntityCheckCurrency() {
	
		String acctId = "123456780000";
		String setCurrency = "EUR";
		
		AccountDto acctDto = new AccountDto.AccountDtoBuilder().setAcctId(acctId).setCurrency(setCurrency).buildDto();
		Account account = accountCreator.newEntity(acctDto);		
		
		AccountPK pK = new AccountPK.AccountPKBuilder().setAcctId(acctId).setCurrency(setCurrency).build();
		Optional<Account> accountOpt = acctRepo.findById(pK);

		boolean present = accountOpt.isPresent();
		
		assertEquals(present, true);

		account = accountOpt.get();
		String currentCurrency = account.getId().getCurrency();
		assertEquals(setCurrency, currentCurrency);
	}
	
	@Test
	@Transactional
	public void createAccountCheckAccountNumber() {
	
		String setAcctId = "003456780000";
		String setCurrency = "EUR";
		
		AccountDto acctDto = new AccountDto.AccountDtoBuilder().setAcctId(setAcctId).setCurrency(setCurrency).buildDto();
		Account account = accountCreator.newEntity(acctDto);		
		
		AccountPK pK = new AccountPK.AccountPKBuilder().setAcctId(setAcctId).setCurrency(setCurrency).build();

		Optional<Account> accountOpt = acctRepo.findById(pK);
		boolean present = accountOpt.isPresent();
		assertEquals(present, true);

		account = accountOpt.get();
		String acctId = account.getId().getAcctId();
		assertEquals(setAcctId, acctId);
	}
	
	@Test
	@Transactional
	public void createWithThreeTheSamePk_ZeroMoney() {
	
		String setAcctId = "003456780009";
		String setCurrency = "EUR";
		
		AccountDto acctDto = new AccountDto.AccountDtoBuilder().setAcctId(setAcctId).setCurrency(setCurrency).buildDto();
		Account accOne = accountCreator.newEntity(acctDto);		
		
		AccountPK pK = new AccountPK.AccountPKBuilder().setAcctId(setAcctId).setCurrency(setCurrency).build();
		Optional<Account> accountOpt = acctRepo.findById(pK);
		assertEquals(accountOpt.isPresent(), true);
		
		acctDto = new AccountDto.AccountDtoBuilder().setAcctId(setAcctId).setCurrency(setCurrency).buildDto();
		Account accTwo = accountCreator.newEntity(acctDto);
		
		assertEquals(accTwo, null);
		
		acctDto = new AccountDto.AccountDtoBuilder().setAcctId(setAcctId).setCurrency(setCurrency).buildDto();
		Account accThree = accountCreator.newEntity(acctDto);

		assertEquals(accThree, null);

		assertNotNull(accOne, "Account One NULL !");
	}
	
	@Test
	@Transactional
	public void create_pk_ThreeTimesTheSame() {
	
		String setAcctId = "003456780009";
		String setCurrency = "EUR";
		BigDecimal amount = new BigDecimal("88.33");
		
		AccountDto acctDto = new AccountDto.AccountDtoBuilder().setAcctId(setAcctId).setCurrency(setCurrency).setAmount(amount).buildDto();
		Account accOne = accountCreator.newEntity(acctDto);
		
		
		AccountPK pK = new AccountPK.AccountPKBuilder().setAcctId(setAcctId).setCurrency(setCurrency).build();
		Optional<Account> accountOpt = acctRepo.findById(pK);
		assertEquals(accountOpt.isPresent(), true);

		acctDto = new AccountDto.AccountDtoBuilder().setAcctId(setAcctId).setCurrency(setCurrency).setAmount(amount).buildDto();
		Account accTwo = accountCreator.newEntity(acctDto);

		assertEquals(accTwo, null);
		

		acctDto = new AccountDto.AccountDtoBuilder().setAcctId(setAcctId).setCurrency(setCurrency).setAmount(amount).buildDto();
		Account accThree = accountCreator.newEntity(acctDto);

		assertEquals(accThree, null);
		
		
		assertNotNull(accOne, "Account One NULL !");
	}	
	
}
