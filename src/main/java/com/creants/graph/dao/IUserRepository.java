package com.creants.graph.dao;

import com.creants.graph.om.User;

/**
 * @author LamHa
 *
 */
public interface IUserRepository {
	User login(String username, String password);

	User login(String uid);

	User getUserInfo(int userId);

	User getUserInfo(String provider, long clientId);

	void insertUser(User user) throws Exception;

	void insertUser(User user, String provider, long clientId, String email);

	int updateUserInfo(int userId, String fullName, int gender, String location, String birthday);

	long incrementUserMoney(int userId, long value);

	boolean checkExistEmail(String email);

	int updatePassword(String email, String newPassword);
}
