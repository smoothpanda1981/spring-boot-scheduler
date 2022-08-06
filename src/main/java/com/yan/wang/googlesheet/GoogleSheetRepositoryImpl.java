package com.yan.wang.googlesheet;

import com.yan.wang.findata.BuySellBtcUsd;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

@Repository
public class GoogleSheetRepositoryImpl implements GoogleSheetRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public String getGoogleSheetContent() {
        System.out.println("3");
        System.out.println("3AAA");
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        System.out.println("3a");
        List<BuySellBtcUsd> buySellBtcUsdList = entityManager.createQuery("Select a From BuySellBtcUsd a", BuySellBtcUsd.class).getResultList();
        System.out.println("size : " + buySellBtcUsdList.size());
        System.out.println("4");
        return "";
    }
}
