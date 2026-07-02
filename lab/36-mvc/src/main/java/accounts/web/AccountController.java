package accounts.web;

import accounts.AccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import rewards.internal.account.Account;

import java.util.List;

/**
 * A Spring MVC REST Controller handling requests to retrieve Account information.
 */
@RestController
public class AccountController {

	private final AccountManager accountManager;

	@Autowired
	public AccountController(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	@GetMapping("/accounts")
	public List<Account> accountList() {
		return accountManager.getAllAccounts();
	}

	@GetMapping("/accounts/{entityId}")
	public Account accountDetails(@PathVariable("entityId") long id) {
		return accountManager.getAccount(id);
	}
}
