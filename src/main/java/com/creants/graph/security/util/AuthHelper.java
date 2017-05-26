package com.creants.graph.security.util;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.creants.graph.om.User;
import com.creants.graph.util.Tracer;

/**
 * https://github.com/auth0/java-jwt
 * 
 * 
 * @author LamHa
 */
public class AuthHelper {
	private static final String ISSUER = "auth0";
	private static final String SIGNING_KEY = "creants@^($%*$%";
	// expire trong 10 ngày
	private static final int TTL_MILI = 864000000;

	public static String createSignToken(long userId, String appId) {
		String token;
		try {
			token = JWT.create().withIssuer(ISSUER).withExpiresAt(new Date(System.currentTimeMillis() + TTL_MILI))
					.withClaim("id", String.valueOf(userId)).withClaim("app_id", appId).withClaim("ttl", TTL_MILI)
					.sign(Algorithm.HMAC256(SIGNING_KEY));
		} catch (Exception e) {
			Tracer.debug(AuthHelper.class, "createJsonWebToken fail!", Tracer.getTraceMessage(e));
			throw new RuntimeException(e);
		}
		return token;
	}

	public static String createSignToken(long userId, String type, String deviceId) {
		String token;
		try {
			token = JWT.create().withIssuer(ISSUER).withExpiresAt(new Date(System.currentTimeMillis() + TTL_MILI))
					.withClaim("id", String.valueOf(userId)).withClaim("type", type).withClaim("device_id", deviceId)
					.withClaim("ttl", TTL_MILI).sign(Algorithm.HMAC256(SIGNING_KEY));
		} catch (Exception e) {
			Tracer.debug(AuthHelper.class, "createJsonWebToken fail!", Tracer.getTraceMessage(e));
			throw new RuntimeException(e);
		}
		return token;
	}

	public static DecodedJWT verifyToken(String token) throws IllegalArgumentException, UnsupportedEncodingException {
		// cho phép trễ 1mili, giả sử set expire là 10mili thì 11mili mới expire
		return JWT.require(Algorithm.HMAC256(SIGNING_KEY)).withIssuer(ISSUER).acceptLeeway(1).build().verify(token);
	}

	public static long getUserId(String token) {
		Claim claim = JWT.decode(token).getClaim("id");
		return Long.parseLong(claim.asString());
	}

	public static User getUser(String token) {
		User user = new User();
		JWT decode = JWT.decode(token);
		user.setUserId(Long.parseLong(decode.getClaim("id").asString()));
		return user;
	}

	public static void main(String[] args) {
		try {
			DecodedJWT verifyToken = verifyToken("eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjMwMCIsImV4cCI6MTQ5NTk2NzI0NywiaXNzIjoiYXV0aDAiLCJhcHBfaWQiOiIxIiwidHRsIjo4NjQwMDAwMDB9.HUnV8SGikTuUel8GTSFI0jPeoDFP8Wx0f2HzwyZSq8M");
			System.out.println("test");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
