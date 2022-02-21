package com.yan.wang.bitstamp;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitstampRepository {

	public void saveBalanceList(List<Balance> balanceList) ;

	public Integer getPagination();

	public Integer getPaginationForIA();

	public List<Balance> getBalanceListByPagination(Integer pageId);

	public void deleteBalance(Balance balance);

	public void dumpOldestPagination();

	public void saveBalanceListInIA(List<BalanceForIA> balanceForIAList);
}