package com.yan.wang.bitstamp;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.ta4j.core.Position;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import ta4jexamples.strategies.MovingMomentumStrategy;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class Ta4jTest2 {

	@Test
	public void getBarSeries() {
		System.out.println("test 1");
		BarSeries series = loadBarSeries("btcusd");
		Strategy strategy = MovingMomentumStrategy.buildStrategy(series);

		/*
		 * Building chart datasets
		 */
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(buildChartTimeSeries(series, new ClosePriceIndicator(series), "Bitstamp Bitcoin (BTC)"));

		/*
		 * Creating the chart
		 */
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Bitstamp BTC", // title
				"Date", // x-axis label
				"Price", // y-axis label
				dataset, // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		);
		XYPlot plot = (XYPlot) chart.getPlot();
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));

		/*
		 * Running the strategy and adding the buy and sell signals to plot
		 */
		addBuySellSignals(series, strategy, plot);

		/*
		 * Displaying the chart
		 */
		displayChart(chart);
		System.out.println("break");
	}

	private BarSeries loadBarSeries(String barSeriesName) {
		BarSeries series = new BaseBarSeriesBuilder().withName(barSeriesName).build();
		String composeUrl = "https://www.bitstamp.net/api/v2/ohlc/btcusd/?step=60&limit=1000";
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
	private static org.jfree.data.time.TimeSeries buildChartTimeSeries(BarSeries barSeries, Indicator<Num> indicator,
																	   String name) {
		org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
		for (int i = 0; i < barSeries.getBarCount(); i++) {
			Bar bar = barSeries.getBar(i);
			chartTimeSeries.add(new Minute(Date.from(bar.getEndTime().toInstant())),
					indicator.getValue(i).doubleValue());
		}
		return chartTimeSeries;
	}

	/**
	 * Runs a strategy over a bar series and adds the value markers corresponding to
	 * buy/sell signals to the plot.
	 *
	 * @param series   the bar series
	 * @param strategy the trading strategy
	 * @param plot     the plot
	 */
	private static void addBuySellSignals(BarSeries series, Strategy strategy, XYPlot plot) {
		// Running the strategy
		BarSeriesManager seriesManager = new BarSeriesManager(series);
		List<Position> positions = seriesManager.run(strategy).getPositions();
		// Adding markers to plot
		for (Position position : positions) {
			// Buy signal
			double buySignalBarTime = new Minute(
					Date.from(series.getBar(position.getEntry().getIndex()).getEndTime().toInstant()))
					.getFirstMillisecond();
			Marker buyMarker = new ValueMarker(buySignalBarTime);
			buyMarker.setPaint(Color.GREEN);
			buyMarker.setLabel("B");
			plot.addDomainMarker(buyMarker);
			// Sell signal
			double sellSignalBarTime = new Minute(
					Date.from(series.getBar(position.getExit().getIndex()).getEndTime().toInstant()))
					.getFirstMillisecond();
			Marker sellMarker = new ValueMarker(sellSignalBarTime);
			sellMarker.setPaint(Color.RED);
			sellMarker.setLabel("S");
			plot.addDomainMarker(sellMarker);
		}
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
		panel.setPreferredSize(new Dimension(1024, 400));
		// Application frame
		ApplicationFrame frame = new ApplicationFrame("Ta4j example - Buy and sell signals to chart");
		frame.setContentPane(panel);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}
}
