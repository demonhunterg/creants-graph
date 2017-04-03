package com.creants.graph.security;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.creants.graph.util.Tracer;

/**
 * https://github.com/auth0/java-jwt
 * 
 * 
 * @author LamHa
 */
public class AuthHelper {
	private static final String ISSUER = "auth0";
	private static final String SIGNING_KEY = "LongAndHardTofdfithSpecialCharacters@^($%*$%";
	// expire trong 1 ngày
	private static final int TTL_MILI = 3600000;

	public static String createSignToken(int userId) {
		String token;
		try {
			token = JWT.create().withIssuer(ISSUER).withExpiresAt(new Date(System.currentTimeMillis() + TTL_MILI))
					.withClaim("id", userId).withClaim("ttl", TTL_MILI).sign(Algorithm.HMAC256(SIGNING_KEY));
		} catch (Exception e) {
			Tracer.debug(AuthHelper.class, "createJsonWebToken fail!", Tracer.getTraceMessage(e));
			throw new RuntimeException(e);
		}
		return token;
	}

	public static String createSignToken(int userId, String type, String deviceId) {
		String token;
		try {
			token = JWT.create().withIssuer(ISSUER).withExpiresAt(new Date(System.currentTimeMillis() + TTL_MILI))
					.withClaim("id", userId).withClaim("type", type).withClaim("device_id", deviceId)
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

	public static int getUserId(String token) {
		return JWT.decode(token).getClaim("id").asInt();
	}

	public static void main(String[] args) {
		String createSignToken = createSignToken(1000);
		System.out.println(createSignToken);
	}
}
