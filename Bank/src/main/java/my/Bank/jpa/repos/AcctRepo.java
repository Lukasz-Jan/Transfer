package my.Bank.jpa.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import my.Bank.entities.Account;
import my.Bank.entities.AccountPK;

@Repository
public interface AcctRepo extends JpaRepository<Account, AccountPK>{


}
