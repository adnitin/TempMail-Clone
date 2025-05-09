package com.tempmail.security;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tempmail.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtTokenUtil jwtTokenUtil;
	private final UserRepository userRepository;
	Logger logger = LoggerFactory.getLogger(JwtFilter.class);

	public JwtFilter(JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
		this.jwtTokenUtil = jwtTokenUtil;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		final String authHeader = request.getHeader("Authorization");

		// Skip filtering for session endPoint
		if (path.equals("/api/session") && request.getMethod().equals("POST")) {
			chain.doFilter(request, response);
			return;
		}

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header!!");
			return;
		}

		try {
			String token = authHeader.substring(7);
			logger.debug("Raw token: {}", token);

			String sessionId = jwtTokenUtil.validateToken(token);
			logger.info("Validated session ID: {}", sessionId);

			if (!userRepository.existsBySessionToken(sessionId)) {
				logger.error("No user found with session token: {}", sessionId);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid session");
				return;
			}

			// Create Authentication object
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(sessionId,
					null, Collections.emptyList() // No authorities
			);
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			request.setAttribute("sessionId", sessionId);
			chain.doFilter(request, response);

		} catch (Exception e) {
			logger.error("JWT validation failed", e);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: " + e.getMessage());
		}
	}
}