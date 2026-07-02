package accounts.client;

import accounts.RestWsApplication;
import common.money.Percentage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rewards.internal.account.Account;
import rewards.internal.account.Beneficiary;

import java.net.URI;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {RestWsApplication.class},
		webEnvironment = WebEnvironment.RANDOM_PORT)
public class AccountClientTests {

	@Autowired
	private TestRestTemplate restTemplate;

	private Random random = new Random();

	@Test
	public void listAccounts_using_invalid_user_should_return_401() {
		ResponseEntity<String> responseEntity
				= restTemplate.withBasicAuth("invalid", "invalid")
				.getForEntity("/accounts", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void listAccounts_using_valid_user_should_succeed() {
		ResponseEntity<Account[]> responseEntity
				= restTemplate.withBasicAuth("user", "user")
				.getForEntity("/accounts", Account[].class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		Account[] accounts = responseEntity.getBody();
		assertThat(accounts.length >= 21).isTrue();
		assertThat(accounts[0].getName()).isEqualTo("Keith and Keri Donald");
		assertThat(accounts[0].getBeneficiaries().size()).isEqualTo(2);
		assertThat(accounts[0].getBeneficiary("Annabelle").getAllocationPercentage()).isEqualTo(Percentage.valueOf("50%"));
	}

	@Test
	public void listAccounts_using_valid_admin_should_succeed() {
		ResponseEntity<Account[]> responseEntity
				= restTemplate.withBasicAuth("admin", "admin")
				.getForEntity("/accounts", Account[].class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void getAccount_using_valid_user_should_succeed() {
		ResponseEntity<Account> responseEntity
				= restTemplate.withBasicAuth("user", "user")
				.getForEntity("/accounts/{accountId}", Account.class, 0);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		Account account = responseEntity.getBody();
		assertThat(account.getName()).isEqualTo("Keith and Keri Donald");
	}

	@Test
	public void createAccount_using_admin_should_return_201() {
		String number = String.format("12345%4d", random.nextInt(10000));
		Account account = new Account(number, "John Doe");
		account.addBeneficiary("Jane Doe");
		ResponseEntity<Void> responseEntity
				= restTemplate.withBasicAuth("admin", "admin")
				.postForEntity("/accounts", account, Void.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	public void createAccount_using_user_should_return_403() {
		String number = String.format("12345%4d", random.nextInt(10000));
		Account account = new Account(number, "John Doe");
		account.addBeneficiary("Jane Doe");
		ResponseEntity<Void> responseEntity
				= restTemplate.withBasicAuth("user", "user")
				.postForEntity("/accounts", account, Void.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	public void addAndDeleteBeneficiary_using_superadmin_should_succeed() {
		URI newBeneficiaryLocation
				= restTemplate.withBasicAuth("superadmin", "superadmin")
				.postForLocation("/accounts/{accountId}/beneficiaries", "David", 1);

		Beneficiary newBeneficiary
				= restTemplate.withBasicAuth("superadmin", "superadmin")
				.getForObject(newBeneficiaryLocation, Beneficiary.class);
		assertThat(newBeneficiary.getName()).isEqualTo("David");

		restTemplate.withBasicAuth("superadmin", "superadmin").delete(newBeneficiaryLocation);

		ResponseEntity<Beneficiary> response =
				restTemplate.withBasicAuth("superadmin", "superadmin")
						.getForEntity(newBeneficiaryLocation, Beneficiary.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
