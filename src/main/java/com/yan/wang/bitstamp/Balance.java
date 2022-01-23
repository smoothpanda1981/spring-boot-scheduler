package com.yan.wang.bitstamp;

public class Balance {

	private String name;

	private String value;

	private String paidPrice;

	private String currentPrice;

	private String profitOrLoss;

	private String priceUpDownPercent;

	private String portfolioName;

	private String last24High;

	private String last24Low;

	private String last24Volume;

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

	public String getLast24High() {
		return last24High;
	}

	public void setLast24High(String last24High) {
		this.last24High = last24High;
	}

	public String getLast24Low() {
		return last24Low;
	}

	public void setLast24Low(String last24Low) {
		this.last24Low = last24Low;
	}

	public String getLast24Volume() {
		return last24Volume;
	}

	public void setLast24Volume(String last24Volume) {
		this.last24Volume = last24Volume;
	}
}
