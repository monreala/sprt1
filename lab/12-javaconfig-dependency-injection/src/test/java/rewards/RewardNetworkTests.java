package rewards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import common.money.MonetaryAmount;

/**
 * A system test that verifies the components of the RewardNetwork application work together to reward for dining
 * successfully. Uses Spring to bootstrap the application for use in a test environment.
 */
public class RewardNetworkTests {

	private RewardNetwork rewardNetwork;

	@BeforeEach
	public void setUp() {
		ApplicationContext context = SpringApplication.run(TestInfrastructureConfig.class);
		rewardNetwork = context.getBean(RewardNetwork.class);
	}

	@Test
	public void testRewardForDining() {
		Dining dining = Dining.createDining("100.00", "1234123412341234", "1234567890");

		RewardConfirmation confirmation = rewardNetwork.rewardAccountFor(dining);

		assertNotNull(confirmation);
		assertNotNull(confirmation.getConfirmationNumber());

		AccountContribution contribution = confirmation.getAccountContribution();
		assertNotNull(contribution);

		assertEquals("123456789", contribution.getAccountNumber());
		assertEquals(MonetaryAmount.valueOf("8.00"), contribution.getAmount());
		assertEquals(2, contribution.getDistributions().size());
		assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Annabelle").getAmount());
		assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Corgan").getAmount());
	}
}
