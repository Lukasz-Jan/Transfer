package my.Bank.services;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import my.Bank.ActionType;
import my.Bank.OutcomeType;
import my.Bank.TransferRequestType;
import my.Bank.entities.Account;
import my.Bank.entities.AccountPK;
import my.Bank.jpa.repos.AcctRepo;


@Component
public class RequestService {

	private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

	@Autowired
	private AcctRepo acctRepo;
	
    @Autowired
    JsonService jsonSrv;
		
	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	public OutcomeType processRequest(TransferRequestType req) {

		AccountPK acctPk = new AccountPK.AccountPKBuilder().setAcctId(req.getTargetAccountNumber())
				.setCurrency(req.getCurrency()).build();
		
		OutcomeType res = acctRepo.findById(acctPk).map(acct -> prosessCreditDebit(acct, req))
				.orElseGet(() -> notFound(req.getRequestId()));
		
		return res;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	private OutcomeType prosessCreditDebit(Account acct, TransferRequestType req) {

		BigDecimal quantity = req.getQuantity();

		if (!quantity.equals(BigDecimal.ZERO)) {

			String accountNumber = req.getTargetAccountNumber();
			BigDecimal currentAmount = acct.getCurAmt();
			
			if (req.getAction() == ActionType.CREDIT) {

				BigDecimal newAmount = currentAmount.add(quantity);
				acct.setCurAmt(newAmount);
				acctRepo.save(acct);
				logger.info("Account " + accountNumber + " income " + quantity + " balance " + newAmount);
				changeJsonFile(accountNumber, req.getCurrency(), newAmount);
				return OutcomeType.ACCEPT;
			} else {

				if (currentAmount.compareTo(quantity) >= 0) {
					BigDecimal newAmount = currentAmount.subtract(quantity);
					acct.setCurAmt(newAmount);
					acctRepo.saveAndFlush(acct);
					logger.info("Account " + accountNumber + " outcome " + quantity + " balance " + newAmount);
					changeJsonFile(accountNumber, req.getCurrency(), newAmount);
					return OutcomeType.ACCEPT;
				} else {
					logger.info("Account " + accountNumber + " not enough funds");
					return OutcomeType.REJECT;
				}
			}
		}
		return OutcomeType.REJECT;
	}
	
	private OutcomeType notFound(String reqId) {
		logger.info("Account not found for request " + reqId);
		return OutcomeType.REJECT;
	}

	private void changeJsonFile(String acct, String currency, BigDecimal newAmount) {
		
		jsonSrv.changeAmountForAccount(acct, currency, newAmount);
	}
}
