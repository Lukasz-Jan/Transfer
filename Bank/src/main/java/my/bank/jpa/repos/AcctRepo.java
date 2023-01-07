package my.bank.jpa.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import my.bank.entities.Account;
import my.bank.entities.AccountPK;

@Repository
public interface AcctRepo extends JpaRepository<Account, AccountPK>{


}
