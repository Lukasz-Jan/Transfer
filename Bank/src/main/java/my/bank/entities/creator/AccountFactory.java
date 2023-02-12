package my.bank.entities.creator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import my.bank.entities.Account;
import my.bank.entities.AccountDto;
import my.bank.jpa.repos.AcctRepo;


@Component
public class AccountFactory {

	public static final Logger log = LoggerFactory.getLogger(AccountFactory.class);

	@Autowired
	private AcctRepo acctRepo;
	
	@Transactional
	public Account newEntity(AccountDto dto) {

		if(!pKCheck(dto)) {
			log.error("Account entity not created");
			return null;
		}

		Account accountEnt = dto.createEntity();		
		Account account = persist(accountEnt);
		return account;
	}

	private Account persist(Account accEnt) {
		
		Account account = null;
		try {
			account = acctRepo.saveAndFlush(accEnt);
		} catch (Exception e) {
			log.error("Account entity creation failed");
		}
		return account;
	}
	

	private boolean pKCheck(AccountDto dto) {

		if(dto.getAcctId() == null || dto.getCurrency() == null) {
			
			log.info("Account no or currrency null - creation failed ");
			return false;
		}
		
		if(dto.getCurrency().length() != 3) {
			log.info("Account entity creation wrong currency ");
			return false;
		}
		return true;
	}
}
