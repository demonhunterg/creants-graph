package com.creants.graph.service;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.creants.graph.util.Security;
import com.creants.graph.util.Tracer;

/**
 * @author LamHa
 *
 */
@Service
public class CacheService {
	@Autowired
	private StringRedisTemplate redisTemplate;


	public void upsert(String key, String jsonString) {
		upsert(key, 0, jsonString);
	}

	public void login(String token, String data) {
		String encryptMD5 = null;
		try {
			encryptMD5 = Security.encryptMD5(token);
			upsert(encryptMD5, 3600, data);
		} catch (NoSuchAlgorithmException e) {
			Tracer.error(this.getClass(), "login fail! token:" + encryptMD5, Tracer.getTraceMessage(e));
		}
	}

	public void upsert(String key, int expireSecond, String jsonString) {
		if (expireSecond > 0) {
			redisTemplate.opsForValue().set(key, jsonString, expireSecond, TimeUnit.SECONDS);
		} else {
			redisTemplate.opsForValue().set(key, jsonString);
		}
	}

	public String get(String key) {
		try {
			return redisTemplate.opsForValue().get(key);
		} catch (Exception e) {
		}

		return null;
	}

	public void delete(String key) {
		try {
			redisTemplate.delete(key);
		} catch (Exception e) {
		}
	}

	public List<String> getBulk(final Collection<String> keys) {
		return redisTemplate.opsForValue().multiGet(keys);
	}
}
