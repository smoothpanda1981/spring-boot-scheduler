package com.yan.wang.findata;

import org.hibernate.Session;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class BuySellBtcUsdRepositoryImpl implements BuySellBtcUsdRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<BuySellBtcUsd> getListOfBuySellBtcUsd() {
        System.out.println("3");
        System.out.println("3AAA");
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        System.out.println("3a");
        List<BuySellBtcUsd> buySellBtcUsdList = entityManager.createQuery("Select a From BuySellBtcUsd a", BuySellBtcUsd.class).getResultList();
        System.out.println("size : " + buySellBtcUsdList.size());
        System.out.println("4");
        return buySellBtcUsdList;
    }
}
