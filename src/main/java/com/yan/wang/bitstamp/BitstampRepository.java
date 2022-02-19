package com.yan.wang.bitstamp;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitstampRepository {

	public void saveBalanceList(List<Balance> balanceList) ;

	public Integer getPagination();
}