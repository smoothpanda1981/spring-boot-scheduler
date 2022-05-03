package com.yan.wang.bitstamp;

import java.util.ArrayList;
import java.util.List;

public class SuperThreeLists {

    private List<Balance> cryptocurrenciesList;

    private List<Balance> cashFlowMainPortfolio;

    private List<Balance> cashFlowProfitsPortfolio;

    public SuperThreeLists() {
        cryptocurrenciesList = new ArrayList<Balance>();
        cashFlowMainPortfolio = new ArrayList<Balance>();
        cashFlowProfitsPortfolio =  new ArrayList<Balance>();
    }

    public List<Balance> getCryptocurrenciesList() {
        return cryptocurrenciesList;
    }

    public void setCryptocurrenciesList(List<Balance> cryptocurrenciesList) {
        this.cryptocurrenciesList = cryptocurrenciesList;
    }

    public List<Balance> getCashFlowMainPortfolio() {
        return cashFlowMainPortfolio;
    }

    public void setCashFlowMainPortfolio(List<Balance> cashFlowMainPortfolio) {
        this.cashFlowMainPortfolio = cashFlowMainPortfolio;
    }

    public List<Balance> getCashFlowProfitsPortfolio() {
        return cashFlowProfitsPortfolio;
    }

    public void setCashFlowProfitsPortfolio(List<Balance> cashFlowProfitsPortfolio) {
        this.cashFlowProfitsPortfolio = cashFlowProfitsPortfolio;
    }
}
