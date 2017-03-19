package com.creants.graph.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.creants.graph.exception.CreantsException;
import com.creants.graph.om.User;
import com.creants.graph.util.Tracer;

/**
 * @author LamHa
 *
 */
@Repository
public class UserRepository implements IUserRepository {
	private static final String[] avatars = { "http://i.imgur.com/PeWm62C.png", "http://i.imgur.com/997n24i.png",
			"http://i.imgur.com/eScEwQI.png", "http://i.imgur.com/qAfV9wE.png", "http://i.imgur.com/SLXXutF.png",
			"http://i.imgur.com/QY8Vbva.png", "http://i.imgur.com/bKR9OK6.png", "http://i.imgur.com/za7t0cm.png",
			"http://i.imgur.com/5wuOP8g.png", "http://i.imgur.com/NZBYPlC.png" };

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public User login(String username, String password) {
		return jdbcTemplate.queryForObject("call sp_account_login(?,?)", new Object[] { username, password },
				new RowMapper<User>() {
					public User mapRow(ResultSet rs, int rowNum) throws SQLException {
						int result = rs.getInt("result");
						if (result == 1) {
							User user = new User();
							user.setId(rs.getInt("user_id"));
							user.setUid(rs.getString("uid"));
							user.setUsername(rs.getString("username"));
							user.setAvatar(rs.getString("avatar"));
							user.setFullName(rs.getString("full_name"));
							user.setMoney(rs.getLong("money"));
							return user;
						}

						return null;
					}
				});
	}

	@Override
	public User login(String uid) {
		return jdbcTemplate.queryForObject("call sp_account_login_uid(?)", new Object[] { uid }, new RowMapper<User>() {
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User user = new User();
				user.setId(rs.getInt("id"));
				user.setUid(rs.getString("user_id"));
				user.setUsername(rs.getString("username"));
				user.setAvatar(rs.getString("avatar"));
				user.setFullName(rs.getString("full_name"));
				user.setMoney(rs.getLong("money"));
				return user;
			}
		});
	}

	@Override
	public User getUserInfo(int userId) {
		try {
			return jdbcTemplate.queryForObject("call sp_account_get_by_id(?)", new Object[] { userId },
					new RowMapper<User>() {
						public User mapRow(ResultSet rs, int rowNum) throws SQLException {
							User user = new User();
							user.setId(rs.getInt("user_id"));
							user.setUid(rs.getString("uid"));
							user.setUsername(rs.getString("username"));
							user.setAvatar(rs.getString("avatar"));
							user.setFullName(rs.getString("full_name"));
							user.setMoney(rs.getLong("money"));
							return user;
						}
					});
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean checkExistEmail(String email) {
		try {
			int result = jdbcTemplate.queryForObject(
					"SELECT id AS user_id FROM account WHERE email = ? AND client_id is null", new Object[] { email },
					new RowMapper<Integer>() {
						@Override
						public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getInt("user_id");
						}
					});
			return result > 0;
		} catch (Exception e) {
			Tracer.error(this.getClass(), "[ERROR] checkExistEmail fail! email: " + email, Tracer.getTraceMessage(e));
		}

		return false;
	}

	@Override
	public User getUserInfo(String provider, long clientId) {
		try {
			return jdbcTemplate.queryForObject("call sp_account_oauth(?,?)", new Object[] { provider, clientId },
					new RowMapper<User>() {
						public User mapRow(ResultSet rs, int rowNum) throws SQLException {
							User user = new User();
							user.setId(rs.getInt("user_id"));
							user.setUid(rs.getString("uid"));
							user.setAvatar(rs.getString("avatar"));
							user.setFullName(rs.getString("full_name"));
							user.setMoney(rs.getLong("money"));
							user.setEmail(rs.getString("email"));
							return user;
						}
					});
		} catch (Exception e) {
			Tracer.error(this.getClass(), "[ERROR] getUserInfo fail! provider: " + provider + ", clientId: " + clientId,
					Tracer.getTraceMessage(e));
		}

		return null;

	}

	@Override
	public void insertUser(final User user) throws Exception {
		jdbcTemplate.query("call sp_account_create(?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new Object[] { user.getUid(), user.getUsername(), user.getPassword(), user.getFullName(),
						avatars[new Random().nextInt(avatars.length - 1)], user.getGender(), user.getLocation(),
						user.getBirthday(), user.getEmail() },
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						int result = rs.getInt("result");
						if (result != 1) {
							throw new SQLException(new CreantsException(result, rs.getString("msg")));
						}

						user.setId(rs.getInt("id"));
					}
				});
	}

	@Override
	public void insertUser(final User user, String provider, long clientId, String email) {
		jdbcTemplate.query("call sp_account_create_social(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new Object[] { user.getUid(), user.getUsername(), user.getPassword(), user.getFullName(),
						user.getAvatar(), user.getGender(), user.getLocation(), user.getBirthday(), provider, clientId,
						email },

				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						int result = rs.getInt("result");
						if (result != 1) {
							throw new SQLException(new CreantsException(result, rs.getString("msg")));
						}

						user.setId(rs.getInt("id"));
					}
				});

	}

	public int updateUserInfo(int id, String lastName) {
		return jdbcTemplate.update(
				"update customer set first_name= COALESCE(?,first_name),last_name = COALESCE(?,last_name) where id = ?",
				null, lastName, id);
	}

	@Override
	public int updateUserInfo(int userId, String fullName, int gender, String location, String birthday) {
		return jdbcTemplate.update("call sp_account_update(?, ?, ?, ?, ?)", fullName, gender, location, birthday,
				userId);
	}

	// TODO update theo userId
	public int updatePassword(String email, String newPassword) {
		return jdbcTemplate.update("update account set password = ? where email = ? AND client_id is null", newPassword,
				email);
	}

	@Override
	public long incrementUserMoney(int userId, long value) {
		return jdbcTemplate.query("call sp_user_update_money(?,?)", new Object[] { userId, value },
				new ResultSetExtractor<Long>() {

					@Override
					public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
						return rs.getLong("result");
					}
				});
	}

}
