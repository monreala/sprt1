package rewards;

import common.money.MonetaryAmount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A system test that verifies the components of the RewardNetwork application
 * work together to reward for dining successfully. Uses Spring to bootstrap the
 * application for use in a test environment.
 */
@SpringJUnitConfig(classes = TestInfrastructureConfig.class)
@ActiveProfiles({"jdbc", "local"})
public class RewardNetworkTests {

	@Autowired
	private RewardNetwork rewardNetwork;

	@Test
	@DisplayName("Test if reward computation and distribution works")
	public void testRewardForDining() {
		Dining dining = Dining.createDining("100.00", "1234123412341234",
				"1234567890");

		RewardConfirmation confirmation = rewardNetwork
				.rewardAccountFor(dining);

		assertNotNull(confirmation);
		assertNotNull(confirmation.getConfirmationNumber());

		AccountContribution contribution = confirmation
				.getAccountContribution();
		assertNotNull(contribution);

		assertEquals("123456789", contribution.getAccountNumber());
		assertEquals(MonetaryAmount.valueOf("8.00"), contribution.getAmount());

		assertAll("distribution of reward",
				() -> assertEquals(2, contribution.getDistributions().size()),
				() -> assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Annabelle").getAmount()),
				() -> assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Corgan").getAmount()));
	}
}
