package com.yan.wang.bitstamp;

import com.yan.wang.findata.BuySellBtcUsd;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
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
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        Integer page = entityManager.createQuery("select max(b.pagination) from Balance b", Integer.class).getSingleResult();
        if (page == null) {
            page = 1;
        } else {
            page = page + 1;
        }
        return page;
    }
}
