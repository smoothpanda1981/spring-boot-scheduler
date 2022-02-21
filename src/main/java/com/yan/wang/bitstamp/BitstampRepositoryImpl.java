package com.yan.wang.bitstamp;

import com.yan.wang.findata.BuySellBtcUsd;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BitstampRepositoryImpl implements BitstampRepository {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    @Transactional
    public void saveBalanceList(List<Balance> balanceList) {
        for (Balance balance : balanceList) {
            entityManager.persist(balance);
        }
    }

    @Override
    @Transactional
    public Integer getPagination() {
        Integer page = entityManager.createQuery("select max(b.pagination) from Balance b", Integer.class).getSingleResult();
        if (page == null) {
            page = 1;
        } else {
            page = page + 1;
        }
        return page;
    }

    @Override
    public Integer getPaginationForIA() {
        Integer page = entityManager.createQuery("select max(b.pagination) from BalanceForIA b", Integer.class).getSingleResult();
        if (page == null) {
            page = 1;
        } else {
            page = page + 1;
        }
        return page;
    }

    @Override
    @Transactional
    public List<Balance> getBalanceListByPagination(Integer pageId) {
        List<Balance> balanceListByPagination = entityManager.createQuery("from Balance b where b.pagination = " + pageId, Balance.class).getResultList();
        return balanceListByPagination;
    }

    @Override
    @Transactional
    public void deleteBalance(Balance balance) {
        if (entityManager.contains(balance)) {
            entityManager.remove(balance);
        } else {
            entityManager.remove(entityManager.merge(balance));
        }
    }

    @Override
    @Transactional
    public void dumpOldestPagination() {
        List<Balance> balanceList = entityManager.createQuery("select b from Balance b", Balance.class).getResultList();
        List<Balance> balanceListWithoutPagination1 = new ArrayList<Balance>();
        for (Balance balance : balanceList) {
            if (balance.getPagination() != 1) {
                Balance newBalance = new Balance();
                newBalance.setName(balance.getName());
                newBalance.setValue(balance.getValue());
                newBalance.setPaidPrice(balance.getPaidPrice());
                newBalance.setCurrentPrice(balance.getCurrentPrice());
                newBalance.setAmountSpendToBuy(balance.getAmountSpendToBuy());
                newBalance.setProfitOrLossValue(balance.getProfitOrLossValue());
                newBalance.setProfitOrLossPercentage(balance.getProfitOrLossPercentage());
                newBalance.setPriceUpDownPercent(balance.getPriceUpDownPercent());
                newBalance.setPortfolioName(balance.getPortfolioName());
                newBalance.setLast24High(balance.getLast24High());
                newBalance.setLast24Low(balance.getLast24Low());
                newBalance.setLast24Volume(balance.getLast24Volume());
                newBalance.setPagination(balance.getPagination()-1);
                newBalance.setDate_pagination(balance.getDate_pagination());

                balanceListWithoutPagination1.add(newBalance);
            }
            deleteBalance(balance);
        }
        saveBalanceList(balanceListWithoutPagination1);
    }

    @Override
    @Transactional
    public void saveBalanceListInIA(List<BalanceForIA> balanceForIAList) {
        for (BalanceForIA balanceForIA : balanceForIAList) {
            entityManager.persist(balanceForIA);
        }
    }
}
