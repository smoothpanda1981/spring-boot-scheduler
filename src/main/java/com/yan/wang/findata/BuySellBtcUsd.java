package com.yan.wang.findata;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.Instant;

@SuppressWarnings("serial")
@Entity
@Table(name="buy_sell_btc_usd")
public class BuySellBtcUsd implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "date")
	private String date;

	@Column(name = "btc_usd_google_trends")
	private int btcUsdGoogleTrends;

	@Column(name = "buy_bitcoin_google_trends")
	private int buyBitcoinGoogleTrends;

	@Column(name = "price")
	private double price;

	@Column(name = "percentage_btc_usd_buy_bitcoin")
	private double percentageBtcUsdBuyBitcoin;

	@Column(name = "decision")
	private String decision;

	@Column(name = "diff_yesterday_today")
	private int diffYesterdayAndToday;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getBtcUsdGoogleTrends() {
		return btcUsdGoogleTrends;
	}

	public void setBtcUsdGoogleTrends(int btcUsdGoogleTrends) {
		this.btcUsdGoogleTrends = btcUsdGoogleTrends;
	}

	public int getBuyBitcoinGoogleTrends() {
		return buyBitcoinGoogleTrends;
	}

	public void setBuyBitcoinGoogleTrends(int buyBitcoinGoogleTrends) {
		this.buyBitcoinGoogleTrends = buyBitcoinGoogleTrends;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getPercentageBtcUsdBuyBitcoin() {
		return percentageBtcUsdBuyBitcoin;
	}

	public void setPercentageBtcUsdBuyBitcoin(double percentageBtcUsdBuyBitcoin) {
		this.percentageBtcUsdBuyBitcoin = percentageBtcUsdBuyBitcoin;
	}

	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	public int getDiffYesterdayAndToday() {
		return diffYesterdayAndToday;
	}

	public void setDiffYesterdayAndToday(int diffYesterdayAndToday) {
		this.diffYesterdayAndToday = diffYesterdayAndToday;
	}
}
