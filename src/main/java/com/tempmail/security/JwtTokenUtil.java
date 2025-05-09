package com.tempmail.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil {

	private static Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private int expiration;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String validateToken(String token) {
		try {
			// Add this verification block
			Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();

			logger.info("JWT Claims - Subject: {}", claims.getSubject());
			logger.info("JWT Claims - IssuedAt: {}", claims.getIssuedAt());
			logger.info("JWT Claims - Expiration: {}", claims.getExpiration());

			// Verify expiration manually if needed
			if (claims.getExpiration().before(new Date())) {
				throw new RuntimeException("Token expired");
			}

			return claims.getSubject();

		} catch (Exception e) {
			logger.error("Token validation failed", e);
			throw new RuntimeException("Invalid token: " + e.getMessage());
		}
	}

	public String generateToken(String sessionId) {
	    String token = Jwts.builder()
	        .setSubject(sessionId)
	        .setIssuedAt(new Date())
	        .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
	        .signWith(getSigningKey())
	        .compact();
	    
	    logger.debug("Generated token for {}: {}", sessionId, token);
	    return token;
	}

}
