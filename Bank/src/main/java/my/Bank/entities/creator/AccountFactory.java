package my.Bank.entities.creator;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import my.Bank.entities.Account;
import my.Bank.entities.AccountPK;
import my.Bank.jpa.repos.AcctRepo;


@Component
public class AccountFactory {

	public static final Logger log = LoggerFactory.getLogger(AccountFactory.class);
	private String acctId;
	private String currency;
	private BigDecimal amount;

	@Autowired
	private AcctRepo acctRepo;

	@Transactional
	public Account buildZeroMoneyEntity() {

		if(!pKCheck()) {
			log.error("Account entity not created");
			return null;
		}
		
		AccountPK accountPk = new AccountPK.AccountPKBuilder().setAcctId(acctId).setCurrency(currency).build();
		Account accountDto = new Account.AccountBuilder().setId(accountPk).setCurAmt(BigDecimal.ZERO)
				.build();

		Account account = persist(accountDto);
		return account;
	}
	
	@Transactional
	public Account buildEntity() {

		if(!pKCheck()) {
			log.error("Account entity not created");
			return null;
		}
		AccountPK accountPk = new AccountPK.AccountPKBuilder().setAcctId(this.acctId).setCurrency(this.currency).build();
		Account accountDto = new Account.AccountBuilder().setId(accountPk).setCurAmt(this.amount)
				.build();

		Account account = persist(accountDto);
		return account;
	}

	private Account persist(Account accDto) {
		
		Account accountEnt = null;
		try {
			accountEnt = acctRepo.saveAndFlush(accDto);
			clearFields();
		} catch (Exception e) {
			log.error("Account entity creation failed");
		}
		finally {
			clearFields();
		}
		return accountEnt;
	}
	
	private boolean pKCheck() {

		if(this.acctId == null || this.currency == null) {
			log.info("Account no or currrency null - creation failed ");
			clearFields();
			return false;
		}
		
		if(this.currency.length() != 3) {
			log.info("Account entity creation wrong currency ");
			clearFields();
			return false;
		}
		return true;
	}
	
	
	
	public AccountFactory setAcctId(String acctId) {
		this.acctId = acctId;
		return this;
	}

	public AccountFactory setCurrency(String currency) {
		this.currency = currency;
		return this;
	}

	public AccountFactory setCurAmt(BigDecimal curAmt) {
		this.amount = curAmt;
		return this;
	}
	
	private void clearFields() {
		
		this.acctId = null;
		this.currency = null;
		this.amount = null;
	}
}
