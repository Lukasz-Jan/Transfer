package my.bank.entities;

import java.math.BigDecimal;

public class AccountDto {

	private String acctId;
	private String currency;
	private BigDecimal amount;
	
	public String getAcctId() {
		return acctId;
	}

	public String getCurrency() {
		return currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	private AccountDto(String acctId, String currency, BigDecimal amount) {
		
		this.acctId = acctId;
		this.currency = currency;
		this.amount = amount;
	}
	
	public Account createEntity() {
		
		AccountPK accountPk = new AccountPK.AccountPKBuilder().setAcctId(acctId).setCurrency(currency).build();
		Account account = new Account.AccountBuilder().setId(accountPk).setCurAmt(this.amount)
				.build();
		
		return account;
	}

	public static class AccountDtoBuilder {
		
		private String acctId;
		private String currency;
		private BigDecimal amount;
		
		public AccountDto buildDto() {
			if(amount == null) amount = BigDecimal.ZERO;
			return new AccountDto(acctId, currency, amount);
		}
		
		public AccountDtoBuilder setAcctId(String acctId) {
			this.acctId = acctId;
			return this;
		}
		
		public AccountDtoBuilder setCurrency(String currency) {
			this.currency = currency;
			return this;
		}
		
		public AccountDtoBuilder setAmount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}
	}
}
