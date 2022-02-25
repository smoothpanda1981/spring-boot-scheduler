package com.yan.wang.bitstamp;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandWidthIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class Ta4jTest {

	//@Test
	public void test1() {
		String result = "{\"data\": {\"pair\": \"BTC/USD\", \"ohlc\": [{\"high\": \"38900.00\", \"timestamp\": \"1645608600\", \"volume\": \"7.68513940\", \"low\": \"38798.58\", \"close\": \"38823.14\", \"open\": \"38811.18\"}, {\"high\": \"38945.73\", \"timestamp\": \"1645609500\", \"volume\": \"9.91656228\", \"low\": \"38784.28\", \"close\": \"38899.29\", \"open\": \"38799.65\"}, {\"high\": \"38950.93\", \"timestamp\": \"1645610400\", \"volume\": \"8.27422173\", \"low\": \"38798.91\", \"close\": \"38813.19\", \"open\": \"38931.22\"}, {\"high\": \"38958.96\", \"timestamp\": \"1645611300\", \"volume\": \"13.70099950\", \"low\": \"38814.89\", \"close\": \"38883.01\", \"open\": \"38814.89\"}, {\"high\": \"38889.39\", \"timestamp\": \"1645612200\", \"volume\": \"0.34363855\", \"low\": \"38859.09\", \"close\": \"38859.09\", \"open\": \"38883.01\"}]}}";
		int openBucket = result.indexOf("[");
		int endBucket = result.indexOf("]");
		String subResult = result.substring(openBucket-1, endBucket+1);
		System.out.println(subResult);
	}

	@Test
	public void getBarSeries() throws IOException {
		System.out.println("test 1");
		BarSeries series = loadBarSeries("btcusd");
		/*
		 * Creating indicators
		 */
		// Close price
		OpenPriceIndicator openPrice = new OpenPriceIndicator(series);
//		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//		EMAIndicator avg14 = new EMAIndicator(closePrice, 14);
//		StandardDeviationIndicator sd14 = new StandardDeviationIndicator(closePrice, 14);
//
//		// Bollinger bands
//		BollingerBandsMiddleIndicator middleBBand = new BollingerBandsMiddleIndicator(avg14);
//		BollingerBandsLowerIndicator lowBBand = new BollingerBandsLowerIndicator(middleBBand, sd14);
//		BollingerBandsUpperIndicator upBBand = new BollingerBandsUpperIndicator(middleBBand, sd14);

		/*
		 * Building chart dataset
		 */
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(buildChartBarSeries(series, openPrice, "BTC USD Open Prise"));
//		dataset.addSeries(buildChartBarSeries(series, closePrice, "BTC USD Close Prise"));
//		dataset.addSeries(buildChartBarSeries(series, lowBBand, "Low Bollinger Band"));
//		dataset.addSeries(buildChartBarSeries(series, upBBand, "High Bollinger Band"));

		/*
		 * Creating the chart
		 */
		JFreeChart chart = ChartFactory.createTimeSeriesChart("BTC Close Prices", // title
				"Date", // x-axis label
				"Price Per Unit", // y-axis label
				dataset, // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		);
		XYPlot plot = (XYPlot) chart.getPlot();
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

		int width = 1028;    /* Width of the image */
		int height = 768;   /* Height of the image */
		File lineChart = new File( "LineChart.jpeg" );
		ChartUtilities.saveChartAsJPEG(lineChart ,chart, width ,height);



		System.out.println("break");
	}

	private BarSeries loadBarSeries(String barSeriesName) {
		BarSeries series = new BaseBarSeriesBuilder().withName(barSeriesName).build();
		String composeUrl = "https://www.bitstamp.net/api/v2/ohlc/btcusd/?step=900&limit=1000";
		URL url = null;
		try {
			url = new URL(composeUrl);
			// Open a connection(?) on the URL(??) and cast the response(???)
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Now it's "open", we can set the request method, headers etc.
			connection.setRequestProperty("accept", "application/json");

			// This line makes the request
			InputStream responseStream = connection.getInputStream();
			String result = IOUtils.toString(responseStream, StandardCharsets.UTF_8);
			int openBucket = result.indexOf("[");
			int endBucket = result.indexOf("]");
			String subResult = result.substring(openBucket-1, endBucket+1);
			InputStream targetStream = new ByteArrayInputStream(subResult.getBytes());


			// Manually converting the response body InputStream to CurrentPrice using Jackson
			ObjectMapper mapper = new ObjectMapper();
			OHLC[] ohlcTab = mapper.readValue(targetStream, OHLC[].class);

			for (OHLC ohlc : ohlcTab) {
				System.out.println(ohlc.getTimestamp() + " - " + ohlc.getClose() + "- " + ohlc.getVolume() + " - " + ohlc.getHigh());
				Date dateToConvert = new Date(Long.valueOf(ohlc.getTimestamp()) * 1000);
				System.out.println(dateToConvert);
				ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(dateToConvert.toInstant(),ZoneId.of("+01:00"));

				System.out.println(zonedDateTime);
				series.addBar(zonedDateTime, ohlc.getOpen(), ohlc.getHigh(), ohlc.getLow(), ohlc.getClose(), ohlc.getVolume());
				System.out.println("---------------------------");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return series;
	}

	/**
	 * Builds a JFreeChart time series from a Ta4j bar series and an indicator.
	 *
	 * @param barSeries the ta4j bar series
	 * @param indicator the indicator
	 * @param name      the name of the chart time series
	 * @return the JFreeChart time series
	 */
	private static org.jfree.data.time.TimeSeries buildChartBarSeries(BarSeries barSeries, Indicator<Num> indicator, String name) {
		org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
		for (int i = 0; i < barSeries.getBarCount(); i++) {
			Bar bar = barSeries.getBar(i);
			System.out.println("bar_endTime : " + bar.getEndTime().toInstant());
			//chartTimeSeries.add(new Hour(Date.from(bar.getEndTime().toInstant())), indicator.getValue(i).doubleValue());
			chartTimeSeries.add(new Minute(Date.from(bar.getEndTime().toInstant())), indicator.getValue(i).doubleValue());
		}
		return chartTimeSeries;
	}

	/**
	 * Displays a chart in a frame.
	 *
	 * @param chart the chart to be displayed
	 */
	private static void displayChart(JFreeChart chart) {
		// Chart panel
		ChartPanel panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
		panel.setMouseWheelEnabled(true);
		panel.setPreferredSize(new java.awt.Dimension(500, 270));
		// Application frame
		ApplicationFrame frame = new ApplicationFrame("Ta4j example - Indicators to chart");
		frame.setContentPane(panel);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}
}
