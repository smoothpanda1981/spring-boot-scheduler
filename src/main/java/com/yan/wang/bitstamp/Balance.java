package com.yan.wang.bitstamp;

import javax.persistence.*;
import java.io.Serializable;

@SuppressWarnings("serial")
@Entity
@Table(name = "balance")
public class Balance implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "crypto_name")
	private String name;

	@Column(name = "crypto_value")
	private String value;

	@Column(name = "paid_price")
	private String paidPrice;

	@Column(name = "current_price")
	private String currentPrice;

	@Column(name = "amount_spend_to_buy")
	private String amountSpendToBuy;

	@Column(name = "profit_or_loss_value")
	private String profitOrLossValue;

	@Column(name = "profit_or_loss_percentage")
	private String profitOrLossPercentage;

	@Column(name = "price_up_down_percentage")
	private String priceUpDownPercent;

	@Column(name = "portfolio_name")
	private String portfolioName;

	@Column(name = "last_24_high")
	private String last24High;

	@Column(name = "last_24_low")
	private String last24Low;

	@Column(name = "last_24_volume")
	private String last24Volume;

	@Column(name = "pagination")
	private Integer pagination;

	@Column(name = "date_pagination")
	private String date_pagination;

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

	public String getAmountSpendToBuy() {
		return amountSpendToBuy;
	}

	public void setAmountSpendToBuy(String amountSpendToBuy) {
		this.amountSpendToBuy = amountSpendToBuy;
	}

	public String getProfitOrLossValue() {
		return profitOrLossValue;
	}

	public void setProfitOrLossValue(String profitOrLossValue) {
		this.profitOrLossValue = profitOrLossValue;
	}

	public String getProfitOrLossPercentage() {
		return profitOrLossPercentage;
	}

	public void setProfitOrLossPercentage(String profitOrLossPercentage) {
		this.profitOrLossPercentage = profitOrLossPercentage;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getPagination() {
		return pagination;
	}

	public void setPagination(Integer pagination) {
		this.pagination = pagination;
	}

	public String getDate_pagination() {
		return date_pagination;
	}

	public void setDate_pagination(String date_pagination) {
		this.date_pagination = date_pagination;
	}
}
