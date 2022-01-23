package com.yan.wang.bitstamp;

public class Balance {

	private String name;

	private String value;

	private String paidPrice;

	private String currentPrice;

	private String profitOrLoss;

	private String priceUpDownPercent;

	private String portfolioName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(String currentPrice) {
		this.currentPrice = currentPrice;
	}

	public String getPaidPrice() {
		return paidPrice;
	}

	public void setPaidPrice(String paidPrice) {
		this.paidPrice = paidPrice;
	}

	public String getProfitOrLoss() {
		return profitOrLoss;
	}

	public void setProfitOrLoss(String profitOrLoss) {
		this.profitOrLoss = profitOrLoss;
	}

	public String getPriceUpDownPercent() {
		return priceUpDownPercent;
	}

	public void setPriceUpDownPercent(String priceUpDownPercent) {
		this.priceUpDownPercent = priceUpDownPercent;
	}

	public String getPortfolioName() {
		return portfolioName;
	}

	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}
}
