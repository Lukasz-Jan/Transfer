package my.Bank;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import my.Bank.entities.Account;
import my.Bank.entities.AccountPK;
import my.Bank.entities.creator.AccountFactory;
import my.Bank.jpa.repos.AcctRepo;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class MainTest {

	@Value("${srcQueue}")
	private String srcQueueName;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private AccountFactory acctCreator;

	@Autowired
	private AcctRepo acctRepo;

	@BeforeAll
	public void init() {

	}

	@Test
	public void queueNameWithContext() {

		assertEquals(srcQueueName, "transferSrcQueue");
	}

	/*
	 * Sends correctly created message jaxB, with Income, checks balance
	 */
	@Test
	public void jaxbSendtMessageIncome() throws InterruptedException {

		String accountId = "100142006678";
		String currency = "SEK";
		BigDecimal amount = new BigDecimal(100);

		Account account = acctCreator.setAcctId(accountId).setCurrency(currency).setCurAmt(amount).buildEntity();
		assertNotNull(account, "account NULL !");
		Optional<Account> accountOpt = acctRepo.findById(account.getId());

		assertEquals(true, accountOpt.isPresent());

		BigDecimal q = new BigDecimal(10.36);
		String req = new Request.Builder().setAcctNo(accountId).setAction(ActionType.CREDIT).setCurrency(currency)
				.setQuantity(q).buildRequestXmlAsString();

		JmsTemplate jms = context.getBean(JmsTemplate.class);
		jms.convertAndSend(srcQueueName, req);

		Thread.sleep(2000);

		accountOpt = acctRepo.findById(account.getId());

		BigDecimal isNow = accountOpt.get().getCurAmt();

		BigDecimal shallBe = new BigDecimal(110.36).setScale(2, RoundingMode.HALF_UP);

		int compare = isNow.compareTo(shallBe);

		assertEquals(0, compare);
	}

	@Test
	public void jaxbSendtMessageOutcome() throws InterruptedException {

		String accountId = "900142006678";
		String currency = "SEK";
		BigDecimal amount = new BigDecimal(100);

		Account account = acctCreator.setAcctId(accountId).setCurrency(currency).setCurAmt(amount).buildEntity();
		assertNotNull(account, "account NULL !");
		Optional<Account> accountOpt = acctRepo.findById(account.getId());

		assertEquals(true, accountOpt.isPresent());

		BigDecimal q = new BigDecimal(10.36);
		String req = new Request.Builder().setAcctNo(accountId).setAction(ActionType.DEBIT).setCurrency(currency)
				.setQuantity(q).buildRequestXmlAsString();

		JmsTemplate jms = context.getBean(JmsTemplate.class);
		jms.convertAndSend(srcQueueName, req);

		Thread.sleep(2000);

		accountOpt = acctRepo.findById(account.getId());

		BigDecimal isNow = accountOpt.get().getCurAmt();

		BigDecimal shallBe = new BigDecimal(89.64).setScale(2, RoundingMode.HALF_UP);

		int compare = isNow.compareTo(shallBe);

		assertEquals(0, compare);
	}

	/*
	 * Sends correctly created message jaxB, with Outcome(debit) below balance
	 * lightly, checks balance
	 */
	@Test
	public void jaxbSendtMessageOutcomeBelowBalance() throws InterruptedException {

		String accountId = "009142006678";
		String currency = "USD";
		BigDecimal amount = new BigDecimal(100);

		Account account = acctCreator.setAcctId(accountId).setCurrency(currency).setCurAmt(amount).buildEntity();
		assertNotNull(account, "account NULL !");
		Optional<Account> accountOpt = acctRepo.findById(account.getId());

		assertEquals(true, accountOpt.isPresent());

		BigDecimal outcome = new BigDecimal(100.01);
		String req = new Request.Builder().setAcctNo(accountId).setAction(ActionType.DEBIT).setCurrency(currency)
				.setQuantity(outcome).buildRequestXmlAsString();

		JmsTemplate jms = context.getBean(JmsTemplate.class);
		jms.convertAndSend(srcQueueName, req);

		Thread.sleep(2000);

		accountOpt = acctRepo.findById(account.getId());

		BigDecimal isNow = accountOpt.get().getCurAmt();

		System.out.println("isNow: " + isNow.toString());

		BigDecimal shallBe = new BigDecimal(100.00).setScale(2, RoundingMode.HALF_UP);

		System.out.println("shallBe: " + shallBe.toString());

		assertEquals(shallBe, isNow);
	}

	/*
	 * Sends distorted message jaxB, with Outcome(debit) , checks balance
	 */
	@Test
	public void jaxbSendtDistortedMessageOutcome() throws InterruptedException {

		String accountId = "099142006678";
		String currency = "USD";
		BigDecimal amount = new BigDecimal(100);

		Account account = acctCreator.setAcctId(accountId).setCurrency(currency).setCurAmt(amount).buildEntity();
		assertNotNull(account, "account NULL !");
		Optional<Account> accountOpt = acctRepo.findById(account.getId());

		assertEquals(true, accountOpt.isPresent());

		BigDecimal outcome = new BigDecimal(50);
		String distortedRequest = new Request.Builder().setAcctNo(accountId).setAction(ActionType.DEBIT)
				.setQuantity(outcome).buildRequestXmlAsString();

		JmsTemplate jms = context.getBean(JmsTemplate.class);
		jms.convertAndSend(srcQueueName, distortedRequest);

		Thread.sleep(2000);

		accountOpt = acctRepo.findById(account.getId());

		BigDecimal isNow = accountOpt.get().getCurAmt();

		System.out.println("isNow: " + isNow.toString());

		BigDecimal shallBe = new BigDecimal(100.00).setScale(2, RoundingMode.HALF_UP);

		System.out.println("shallBe: " + shallBe.toString());

		assertEquals(shallBe, isNow);
	}

	/*
	 * Sends message jaxB, with Outcome(debit) three times, checks balance
	 */
	@Test
	public void debitThreeTimes() throws InterruptedException {

		String accountId = "099142006678";
		String currency = "EUR";
		BigDecimal amount = new BigDecimal(100000);

		Account account = acctCreator.setAcctId(accountId).setCurrency(currency).setCurAmt(amount).buildEntity();

		assertNotNull(account, "account NULL !");
		Optional<Account> accountOpt = acctRepo.findById(account.getId());

		assertEquals(true, accountOpt.isPresent());

		BigDecimal outcome = new BigDecimal(555.99);
		String request = new Request.Builder().setAcctNo(accountId).setAction(ActionType.DEBIT).setCurrency(currency)
				.setQuantity(outcome).buildRequestXmlAsString();

		JmsTemplate jms = context.getBean(JmsTemplate.class);
		jms.convertAndSend(srcQueueName, request);
		jms.convertAndSend(srcQueueName, request);
		jms.convertAndSend(srcQueueName, request);

		Thread.sleep(2000);

		accountOpt = acctRepo.findById(account.getId());

		BigDecimal isNow = accountOpt.get().getCurAmt();

		BigDecimal shallBe = new BigDecimal(98332.03).setScale(2, RoundingMode.HALF_UP);

		assertEquals(shallBe, isNow);

	}

	/*
	 * Sends message jaxB, to account from Json, three times, checks balance
	 */
	@Test
	public void outcomeFromJSonAccount() throws InterruptedException {

		String accountId = "100056013005";
		String currency = "USD";

		AccountPK pK = new AccountPK.AccountPKBuilder().setAcctId(accountId).setCurrency(currency).build();	
		Account account = acctRepo.findById(pK).map(a -> {return a;}).orElse(null);

		assertNotNull(account, "account NULL !");

		BigDecimal outcome = new BigDecimal(155.22);
		String request = new Request.Builder().setAcctNo(accountId).setAction(ActionType.DEBIT).setCurrency(currency)
				.setQuantity(outcome).buildRequestXmlAsString();

		JmsTemplate jms = context.getBean(JmsTemplate.class);
		jms.convertAndSend(srcQueueName, request);
		jms.convertAndSend(srcQueueName, request);
		jms.convertAndSend(srcQueueName, request);		
	
		Thread.sleep(2000);

		Optional<Account> accountOpt = acctRepo.findById(account.getId());

		BigDecimal isNow = accountOpt.get().getCurAmt();

		BigDecimal shallBe = new BigDecimal(49534.34).setScale(2, RoundingMode.HALF_UP);

		assertEquals(shallBe, isNow);
	}
	

}