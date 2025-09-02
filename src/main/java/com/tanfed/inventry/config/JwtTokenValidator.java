package com.tanfed.inventry.config;

import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tanfed.inventry.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenValidator extends OncePerRequestFilter {

	private static Logger logger = LoggerFactory.getLogger(JwtTokenValidator.class);

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			String jwt = request.getHeader(JwtConstant.JWT_HEADER);
			if (jwt != null) {
				String token = jwt.substring(7);

//				logger.info("BlockList {}", blockListJwt());
				boolean isBlocked = blockListJwt().contains(jwt);
				if (isBlocked) {
					throw new BadCredentialsException("Blocked token...");
				}
				SecretKey key = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

				Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

				String email = String.valueOf(claims.get("empId"));
				String authorities = String.valueOf(claims.get("authorities"));

				List<GrantedAuthority> auths = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
				Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, auths);

				logger.info("{}", auths);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception e) {
			throw new BadCredentialsException("Invalid token..." + e);
		}

		filterChain.doFilter(request, response);
	}

	private List<String> blockListJwt() {
		UserService userService = SpringContextUtil.getBean(UserService.class);
		return userService.getBlockedJwtList();
	}

	public static String getEmailFromJwtToken(String jwt) {
		SecretKey key = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());
		jwt = jwt.substring(7);
		Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();

		String email = String.valueOf(claims.get("empId"));
		return email;
	}
}
