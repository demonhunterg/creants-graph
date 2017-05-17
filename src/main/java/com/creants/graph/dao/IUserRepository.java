package com.creants.graph.dao;

import java.util.List;

import com.creants.graph.om.User;

/**
 * @author LamHa
 *
 */
public interface IUserRepository {
	User login(String username, String password);


	User loginByGuest(String deviceId);


	User getUserInfo(long userId);


	User getUserInfo(String provider, long clientId);


	void insertUser(User user) throws Exception;


	void insertGuest(User user) throws Exception;


	void insertUser(User user, String provider, long clientId);


	int updateUserInfo(long userId, String fullName, int gender, String location, String birthday);


	int changePassword(long userId, String password, String newPassword);


	void linkAccountFb(long userId, User user, long fbClientId);


	long incrementUserMoney(long userId, long value);


	boolean checkExistEmail(String email);


	int updatePassword(String email, String newPassword);


	List<User> findUserList(String userIds);

}
