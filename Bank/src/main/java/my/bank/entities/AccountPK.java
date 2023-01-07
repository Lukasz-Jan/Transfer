package my.bank.entities;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the CI_ACCOUNT database table.
 * 
 */
@Embeddable
public class AccountPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="ACCT_ID", nullable=false, length=12)
	private String acctId;

	@Column(name="CURRENCY", nullable=false, length=3)
	private String currency;

	public AccountPK() {
	}
	
	
	public AccountPK(String acctId, String currency) {
		this.acctId = acctId;
		this.currency = currency;
	}


	public String getAcctId() {
		return this.acctId;
	}
	public void setAcctId(String acctId) {
		this.acctId = acctId;
	}
	public String getCurrency() {
		return this.currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AccountPK)) {
			return false;
		}
		AccountPK castOther = (AccountPK)other;
		return 
			this.acctId.equals(castOther.acctId)
			&& this.currency.equals(castOther.currency);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.acctId.hashCode();
		hash = hash * prime + this.currency.hashCode();
		
		return hash;
	}
	
	public static class AccountPKBuilder {

		private String acctId;
		private String currency;
		
		public AccountPKBuilder() {}

		public AccountPKBuilder setAcctId(String acctId) {
			this.acctId = acctId;
			return this;
		}

		public AccountPKBuilder setCurrency(String currency) {
			this.currency = currency;
			return this;
		}

		public AccountPK build() {
			return new AccountPK(this.acctId, this.currency);
		}
	}	
}