package rewards.internal.restaurant;

import common.money.Percentage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import rewards.Dining;
import rewards.internal.account.Account;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Loads restaurants from a data source using the JDBC API.
 */
public class JdbcRestaurantRepository implements RestaurantRepository {

	private JdbcTemplate jdbcTemplate;

	public JdbcRestaurantRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<Restaurant> rowMapper = new RestaurantRowMapper();

	public Restaurant findByMerchantNumber(String merchantNumber) {
		String sql = "select MERCHANT_NUMBER, NAME, BENEFIT_PERCENTAGE, BENEFIT_AVAILABILITY_POLICY"
				+ " from T_RESTAURANT where MERCHANT_NUMBER = ?";
		return jdbcTemplate.queryForObject(sql, rowMapper, merchantNumber);
	}

	private Restaurant mapRestaurant(ResultSet rs) throws SQLException {
		String name = rs.getString("NAME");
		String number = rs.getString("MERCHANT_NUMBER");
		Percentage benefitPercentage = Percentage.valueOf(rs.getString("BENEFIT_PERCENTAGE"));

		Restaurant restaurant = new Restaurant(number, name);
		restaurant.setBenefitPercentage(benefitPercentage);
		restaurant.setBenefitAvailabilityPolicy(mapBenefitAvailabilityPolicy(rs));
		return restaurant;
	}

	private BenefitAvailabilityPolicy mapBenefitAvailabilityPolicy(ResultSet rs) throws SQLException {
		String policyCode = rs.getString("BENEFIT_AVAILABILITY_POLICY");
		if ("A".equals(policyCode)) {
			return AlwaysAvailable.INSTANCE;
		} else if ("N".equals(policyCode)) {
			return NeverAvailable.INSTANCE;
		} else {
			throw new IllegalArgumentException("Not a supported policy code " + policyCode);
		}
	}

	static class AlwaysAvailable implements BenefitAvailabilityPolicy {
		static final BenefitAvailabilityPolicy INSTANCE = new AlwaysAvailable();

		public boolean isBenefitAvailableFor(Account account, Dining dining) {
			return true;
		}

		public String toString() {
			return "alwaysAvailable";
		}
	}

	static class NeverAvailable implements BenefitAvailabilityPolicy {
		static final BenefitAvailabilityPolicy INSTANCE = new NeverAvailable();

		public boolean isBenefitAvailableFor(Account account, Dining dining) {
			return false;
		}

		public String toString() {
			return "neverAvailable";
		}
	}

	private class RestaurantRowMapper implements RowMapper<Restaurant> {

		public Restaurant mapRow(ResultSet rs, int rowNum) throws SQLException {
			return mapRestaurant(rs);
		}
	}
}
