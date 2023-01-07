package my.bank.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the CI_ACCOUNT database table.
 * 
 */
@Entity
@Table(name="CI_ACCOUNT")
@NamedQuery(name="Account.findAll", query="SELECT a FROM Account a")
public class Account implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private AccountPK id;

	@Column(name="CUR_AMT", precision=19, scale=2)
	private BigDecimal curAmt;

	@Version
	@Column(name="VERSION", precision=19, scale=2)
	private Long version;

	public Account() {
	}

	public Account(AccountPK id, BigDecimal curAmt) {
		this.id = id;
		this.curAmt = curAmt;
	}

	public AccountPK getId() {
		return this.id;
	}

	public void setId(AccountPK id) {
		this.id = id;
	}

	public BigDecimal getCurAmt() {
		return this.curAmt;
	}

	public void setCurAmt(BigDecimal curAmt) {
		this.curAmt = curAmt;
	}

	public Long getVersion() {
		return this.version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public static class AccountBuilder {

		private AccountPK id;
		private BigDecimal curAmt;
		private Long version;

		public AccountBuilder setId(AccountPK id) {
			this.id = id;
			return this;
		}

		public AccountBuilder setCurAmt(BigDecimal curAmt) {
			this.curAmt = curAmt;
			return this;
		}

		public AccountBuilder setVersion(Long version) {
			this.version = version;
			return this;
		}

		public Account build() {
			return new Account(this.id, this.curAmt);
		}
	}
}