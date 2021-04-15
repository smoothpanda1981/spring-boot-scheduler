package com.yan.wang.findata;

import com.yan.wang.account.Account;
import com.yan.wang.account.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
public class BuySellBtcUsdService {
	
	@Autowired
	private BuySellBtcUsdRepository buySellBtcUsdRepository;

	public List<BuySellBtcUsd> getListOfBuySellBtcUsd() {
		System.out.println("2");
		return buySellBtcUsdRepository.getListOfBuySellBtcUsd();
	}
}
