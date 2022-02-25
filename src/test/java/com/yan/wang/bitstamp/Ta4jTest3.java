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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
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
public class Ta4jTest3 {

	@Test
	public void getBarSeries() {
		System.out.println("test 1");
		OHLC[] ohlcs = loadList("btcusd");

		JFreeChart lineChart = ChartFactory.createLineChart(
				"HEllo",
				"Years","Number of Schools",
				createDataset(ohlcs),
				PlotOrientation.VERTICAL,
				true,true,false);

		displayChart(lineChart);
	}

	private static void displayChart(JFreeChart chart) {
		// Chart panel
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
		// Application frame
		ApplicationFrame frame = new ApplicationFrame("Ta4j example - Indicators to chart");
		frame.setContentPane(panel);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}

	private DefaultCategoryDataset createDataset(OHLC[] ohlcs) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
		for (OHLC ohlc : ohlcs) {
			dataset.addValue(Double.parseDouble(ohlc.getOpen()), "Timeline" , "value" );
		}
		return dataset;
	}

	private OHLC[] loadList(String crypto) {
		OHLC[] ohlcTab = null;
		String composeUrl = "https://www.bitstamp.net/api/v2/ohlc/" + crypto +"/?step=3600&limit=200";
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
			ohlcTab = mapper.readValue(targetStream, OHLC[].class);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ohlcTab;
	}
}
