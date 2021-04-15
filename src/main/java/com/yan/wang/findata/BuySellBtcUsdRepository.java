package com.yan.wang.findata;

import com.yan.wang.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuySellBtcUsdRepository {

	public List<BuySellBtcUsd> getListOfBuySellBtcUsd();
}