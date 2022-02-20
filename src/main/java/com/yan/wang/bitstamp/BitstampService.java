package com.yan.wang.bitstamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BitstampService {
	
	@Autowired
	private BitstampRepository bitstampRepository;

	public void saveBalanceList(List<Balance> balanceList) {
		bitstampRepository.saveBalanceList(balanceList);
	}

	public Integer getPagination() {
		return bitstampRepository.getPagination();
	}

	public List<Balance> getBalanceListByPagination(Integer pageId) {
		return bitstampRepository.getBalanceListByPagination(pageId);
	}

	public void deleteBalance(Balance balance) {
		bitstampRepository.deleteBalance(balance);
	}

	public void dumpOldestPagination() {
		bitstampRepository.dumpOldestPagination();
	}
}
