package com.creants.graph.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.creants.graph.dao.AccountRepository;
import com.creants.graph.om.User;

/**
 * @author LamHM
 *
 */
@Service
public class AccountManager {
	@Autowired
	private AccountRepository accountRepo;
	@Autowired
	private AutoIncrementService incrService;


	public User getUserInfo(String provider, String clientId) {
		return accountRepo.getByProviderAndClientId(provider, clientId);
	}


	public User getUserInfo(long userId) {
		return accountRepo.getUserInfo(userId);
	}


	public void insertUser(User userInfo) {
		userInfo.setId(incrService.genAccountId());
		userInfo.setCreateTime(new Date(System.currentTimeMillis()));
		accountRepo.save(userInfo);
	}


	public User login(String username, String md5Password) {
		return accountRepo.getUserInfo(username, md5Password);
	}


	public boolean updatePassword(String email, String password) {
		return false;
	}
}
