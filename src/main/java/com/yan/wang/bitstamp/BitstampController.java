package com.yan.wang.bitstamp;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.Num;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.jfree.chart.ChartUtilities.writeChartAsJPEG;

@Controller
public class BitstampController {

    @Autowired
    private BitstampService bitstampService;

    @GetMapping("/bitstamp")
    public ModelAndView getGeneralData() {

        try {
            Properties props = new Properties();
            props.load(new FileInputStream("/home/ywang/bitstamp/bitstampapi.properties"));
            String result = getMainBalance(props).replace(" ", "");

            List<Balance> balanceList = getBalanceList(result);
            List<Balance> balanceList2 = getBalanceList2(balanceList);
            List<Balance> balanceList3 = getBalanceList3(balanceList2, props);
            List<Balance> balanceList4 = getBalanceList4(balanceList3, props);


            List<Balance> cryptocurrenciesList = new ArrayList<Balance>();
            List<Balance> cashFlowMainPortfolio = new ArrayList<Balance>();
            List<Balance> cashFlowProfitsPortfolio = new ArrayList<Balance>();
            Integer pagination = bitstampService.getPagination();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT+01:00"));
            String datePagination = dtf.format(now);

            for (Balance balance : balanceList4) {
                Balance balance5 = new Balance();
                balance5.setName(balance.getName());

                Double doubleValue1 = Double.parseDouble(balance.getValue());
                balance5.setValue(String.format("%.3f", doubleValue1));

                balance5.setCurrentPrice(balance.getCurrentPrice());
                balance5.setLast24Low(balance.getLast24Low());
                balance5.setLast24High(balance.getLast24High());


                String[] tab = new String[2];
                tab = balance.getLast24Volume().split("\\.");
                balance5.setLast24Volume(tab[0]);

                balance5.setPaidPrice(balance.getPaidPrice());
                balance5.setAmountSpendToBuy(balance.getAmountSpendToBuy());
                balance5.setProfitOrLossValue(balance.getProfitOrLossValue());
                balance5.setProfitOrLossPercentage(balance.getProfitOrLossPercentage());
                balance5.setPriceUpDownPercent(balance.getPriceUpDownPercent());
                balance5.setPortfolioName(balance.getPortfolioName());

                // add pagination
                balance5.setPagination(pagination);
                balance5.setDate_pagination(datePagination);

                if (balance.getName().startsWith("usd_") || balance.getName().startsWith("eur_")) {
                    if (balance.getPortfolioName().equals("Main")) {
                        cashFlowMainPortfolio.add(balance5);
                    } else {
                        cashFlowProfitsPortfolio.add(balance5);
                    }
                } else {
                    cryptocurrenciesList.add(balance5);
                }
            }

            List<UserTransaction> userTransactionList = new ArrayList<UserTransaction>();
            List<UserTransaction> userTransactionBoughtList = new ArrayList<UserTransaction>();
            String resultTemp = getUserTransactions(props).replace(" ", "");
            String resultSub = resultTemp.substring(2, resultTemp.length()-2);
            String[] resultSubTab = resultSub.split("\\},\\{");
            for (int i = 0; i < resultSubTab.length; i++) {
                System.out.println("resultSubTab[" + i + "] :" + resultSubTab[i]);
                if (resultSubTab[i].contains("\"type\":\"2\"")
                        && resultSubTab[i].contains("\"order_id\"")
                        && resultSubTab[i].contains("\"fee\"")
                        && resultSubTab[i].contains("\"usd\"")
                        && !resultSubTab[i].contains("\"bch\"")
                        && !resultSubTab[i].contains("_eur\"")
                        && !resultSubTab[i].contains("_btc\"")) {
                    String[] oneRowTab = resultSubTab[i].split(",");
                    boolean show = false;
                    for (int j = 0; j < oneRowTab.length; j++) {
                        if (oneRowTab[j].startsWith("\"usd\":\"")) {
                            String[] subString = oneRowTab[j].split(":");
                            String value = subString[1].substring(1, subString[1].length()-1);
                            Double d = Double.parseDouble(value);
                            if (d > 0) {
                                show = true;
                            }
                        }
                    }

                        UserTransaction userTransaction = new UserTransaction();
                        String userTransactionString = resultSubTab[i];
                        String[] userTransactionTab = userTransactionString.split(",");
                        for (int k = 0; k < userTransactionTab.length; k++) {
                            boolean goToElse = true;
                            String userTransactionObj = userTransactionTab[k];
                            String[] userTransactionObjTab = userTransactionObj.split(":");
                            if (userTransactionObjTab[0].equals("\"usd\"")) {
                                Double usd = Double.parseDouble(userTransactionObjTab[1].substring(1, userTransactionObjTab[1].length() - 1));
                                DecimalFormat df = new DecimalFormat("0.00");
                                userTransaction.setUsd(df.format(usd));
                            } else if (userTransactionObjTab[0].equals("\"order_id\"")) {
                                userTransaction.setOrderId(userTransactionObjTab[1]);
                            } else if (userTransactionObjTab[0].contains("_usd")) {
                                Double cryptoUsd = Double.parseDouble(userTransactionObjTab[1]);
                                userTransaction.setCryptoUsd(String.format("%.5f", cryptoUsd));
                                userTransaction.setCryptoUsdName(userTransactionObjTab[0].substring(1, userTransactionObjTab[0].length() - 1));
                            } else if (userTransactionObjTab[0].equals("\"datetime\"")) {
                                userTransaction.setDatetime(userTransactionObjTab[1].substring(1, userTransactionObjTab[1].length() - 2));
                            } else if (userTransactionObjTab[0].equals("\"fee\"")) {
                                Double fee = Double.parseDouble(userTransactionObjTab[1].substring(1, userTransactionObjTab[1].length() - 1));
                                DecimalFormat df = new DecimalFormat("0.00");
                                userTransaction.setFee(df.format(fee));
                            } else if (userTransactionObjTab[0].equals("\"btc\"")) {
                                userTransaction.setBtc(userTransactionObjTab[1]);
                                if (!userTransactionObjTab[1].equals("0.0")) {
                                    goToElse = false;
                                    Double cryptoAmount = Double.parseDouble(userTransactionObjTab[1].substring(1, userTransactionObjTab[1].length() - 1));
                                    userTransaction.setCryptoAmount(String.format("%.4f", cryptoAmount));
                                    userTransaction.setCryptoAmountName(userTransactionObjTab[0].substring(1, userTransactionObjTab[0].length() - 1));
                                }
                            } else if (userTransactionObjTab[0].equals("\"type\"")) {
                                userTransaction.setType(userTransactionObjTab[1].substring(1, userTransactionObjTab[1].length() - 1));
                            } else if (userTransactionObjTab[0].equals("\"id\"")) {
                                userTransaction.setId(userTransactionObjTab[1]);
                            } else if (userTransactionObjTab[0].equals("\"eur\"")) {
                                userTransaction.setEur(userTransactionObjTab[1]);
                            } else {
                                if (goToElse) {
                                    Double cryptoAmount = Double.parseDouble(userTransactionObjTab[1].substring(1, userTransactionObjTab[1].length() - 1));
                                    userTransaction.setCryptoAmount(String.format("%.4f", cryptoAmount));
                                    userTransaction.setCryptoAmountName(userTransactionObjTab[0].substring(1, userTransactionObjTab[0].length() - 1));
                                }
                            }
                        }
                        if (show) {
                            userTransactionList.add(userTransaction);
                        } else {
                            userTransactionBoughtList.add(userTransaction);
                        }
                } else {
                    System.out.println("excluded => resultSubTab[" + i + "] :" + resultSubTab[i]);
                }
            }

            Set<String> cryptoNameSet = new HashSet<String>();
            for (UserTransaction userTransaction : userTransactionList) {
                String key = userTransaction.getCryptoUsdName();
                if (!cryptoNameSet.contains(key)) {
                    cryptoNameSet.add(key);
                }
            }
            List<String> cryptoNameList = List.copyOf(cryptoNameSet);

            Set<String> cryptoNameSetBought = new HashSet<String>();
            for (UserTransaction userTransaction : userTransactionBoughtList) {
                String key = userTransaction.getCryptoUsdName();
                if (!cryptoNameSetBought.contains(key)) {
                    cryptoNameSetBought.add(key);
                }
            }
            List<String> cryptoNameBoughtList = List.copyOf(cryptoNameSetBought);

            Set<String> boughtAndSoldset = new LinkedHashSet<>(cryptoNameList);
            boughtAndSoldset.addAll(cryptoNameBoughtList);
            List<String> cryptoNameBoughtAndSoldList = new ArrayList<>(boughtAndSoldset);

            cryptoNameBoughtAndSoldList = cryptoNameBoughtAndSoldList.stream().sorted().collect(Collectors.toList());

            List<UserTransaction> sortedUserTransactionList = new ArrayList<UserTransaction>();
            for (UserTransaction userTransaction : userTransactionList) {
                String cryptoUsdName = userTransaction.getCryptoUsdName();
                if (cryptoNameSet.contains(cryptoUsdName)) {
                    sortedUserTransactionList.add(userTransaction);
                    cryptoNameSet.remove(cryptoUsdName);
                }
            }

            for (UserTransaction userTransaction : sortedUserTransactionList) {
                String crypto = userTransaction.getCryptoUsdName().replace("_", "");
                Ticker ticker = getTicker(crypto);
                userTransaction.setCurrentPrice(ticker.getLast());

                Double soldPrice = Double.parseDouble(userTransaction.getCryptoUsd());
                Double currentPrice = Double.parseDouble(userTransaction.getCurrentPrice());
                Double fee = Double.parseDouble(userTransaction.getFee());
                Double amount = Double.parseDouble(userTransaction.getCryptoAmount());
                Double profitAndLoss = ((currentPrice * amount) - fee) - (soldPrice * amount) + fee;
                DecimalFormat df = new DecimalFormat("0.00");
                userTransaction.setProfitAndLoss(df.format(profitAndLoss));
                if (profitAndLoss > 0.00) {
                    userTransaction.setProfitAndLossFlag("green");
                } else if (profitAndLoss == 0.00) {
                    userTransaction.setProfitAndLossFlag("black");
                } else {
                    userTransaction.setProfitAndLossFlag("red");
                }
            }


            List<UserTransaction> sortedUserTransactionBoughtList = new ArrayList<UserTransaction>();
            for (UserTransaction userTransaction : userTransactionBoughtList) {
                String cryptoUsdName = userTransaction.getCryptoUsdName();
                if (cryptoNameSetBought.contains(cryptoUsdName)) {
                    sortedUserTransactionBoughtList.add(userTransaction);
                    cryptoNameSetBought.remove(cryptoUsdName);
                }
            }

            for (UserTransaction userTransaction : sortedUserTransactionBoughtList) {
                String crypto = userTransaction.getCryptoUsdName().replace("_", "");
                Ticker ticker = getTicker(crypto);
                userTransaction.setCurrentPrice(ticker.getLast());

                Double soldPrice = Double.parseDouble(userTransaction.getCryptoUsd());
                Double currentPrice = Double.parseDouble(userTransaction.getCurrentPrice());
                Double fee = Double.parseDouble(userTransaction.getFee());
                Double amount = Double.parseDouble(userTransaction.getCryptoAmount());
                Double profitAndLoss = ((currentPrice * amount) - fee) - (soldPrice * amount) + fee;
                DecimalFormat df = new DecimalFormat("0.00");
                userTransaction.setProfitAndLoss(df.format(profitAndLoss));
                if (profitAndLoss > 0.00) {
                    userTransaction.setProfitAndLossFlag("green");
                } else if (profitAndLoss == 0.00) {
                    userTransaction.setProfitAndLossFlag("black");
                } else {
                    userTransaction.setProfitAndLossFlag("red");
                }
            }

            bitstampService.saveBalanceList(cryptocurrenciesList);
            List<BalanceForIA> balanceForIAList = copyBalanceListIntoBalanceForIAList(cryptocurrenciesList);
            bitstampService.saveBalanceListInIA(balanceForIAList);


            ModelAndView modelAndView = new ModelAndView("bitstamp/bitstamp");
            modelAndView.addObject("crytocurrenciesList", cryptocurrenciesList);
            modelAndView.addObject("cashFlowMainPortfolio", cashFlowMainPortfolio);
            modelAndView.addObject("cashFlowProfitsPortfolio", cashFlowProfitsPortfolio);

            int sortedUserTransactionListSize = sortedUserTransactionList.size();
            int sortedUserTransactionBoughtListSize = sortedUserTransactionBoughtList.size();
            if (sortedUserTransactionListSize > sortedUserTransactionBoughtListSize) {
                modelAndView.addObject("sortedUserTransactionSoldList", sortedUserTransactionList.subList(0, sortedUserTransactionBoughtListSize));
                modelAndView.addObject("sortedUserTransactionBoughtList", sortedUserTransactionBoughtList);
            } else if (sortedUserTransactionListSize < sortedUserTransactionBoughtListSize) {
                modelAndView.addObject("sortedUserTransactionSoldList", sortedUserTransactionList);
                modelAndView.addObject("sortedUserTransactionBoughtList", sortedUserTransactionBoughtList.subList(0, sortedUserTransactionListSize));
            } else {
                modelAndView.addObject("sortedUserTransactionSoldList", sortedUserTransactionList);
                modelAndView.addObject("sortedUserTransactionBoughtList", sortedUserTransactionBoughtList);
            }


            if (pagination == 12) {
                // delete in database
                bitstampService.dumpOldestPagination();
                pagination = 11;
            }
            Integer paginationMinusOne = pagination - 1;
            modelAndView.addObject("pagination", paginationMinusOne);

            Map<String, Integer> minuteurList = new LinkedHashMap<String, Integer>();
            minuteurList.put("1 min", 60);
            minuteurList.put("3 min", 180);
            minuteurList.put("5 min", 300);
            minuteurList.put("15 min", 900);
            minuteurList.put("30 min", 1800);
            minuteurList.put("1 H", 3600);
            minuteurList.put("2 H", 7200);
            minuteurList.put("3 H", 14400);
            minuteurList.put("6 H", 21600);
            minuteurList.put("12 H", 43200);
            minuteurList.put("1 D", 86400);
            minuteurList.put("3 D", 259200);
            modelAndView.addObject("step", minuteurList);
            StepBalance balance = new StepBalance();
            balance.setPagination(60);;
            modelAndView.addObject("paidPriceObject", balance);

            modelAndView.addObject("cryptoNameBoughtAndSoldList", cryptoNameBoughtAndSoldList);


            return modelAndView;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = {"/bitstamp/{cryptoId}/edit"})
    public ModelAndView showEditCryptoId(@PathVariable String cryptoId) {
        Properties props = new Properties();
        Balance balance = new Balance();
        try {
            props.load(new FileInputStream("/home/ywang/bitstamp/bitstampapi.properties"));
            if (props.getProperty(cryptoId.replace("_balance", "") + ".paid.price") !=  null) {
                balance.setName(cryptoId);
                balance.setValue(cryptoId.replace("_balance", "") + ".paid.price");
                balance.setPaidPrice(props.getProperty(cryptoId.replace("_balance", "") + ".paid.price"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ModelAndView modelAndView = new ModelAndView("bitstamp/edit");
            modelAndView.addObject("paidPriceObject", balance);
            return modelAndView;
        }
    }

    @GetMapping(value = {"/bitstamp/{cryptoId}/add"})
    public ModelAndView showAddCryptoId(@PathVariable String cryptoId) {
        Balance balance = new Balance();
        balance.setName(cryptoId);
        balance.setValue(cryptoId.replace("_balance", "") + ".paid.price");
        balance.setPaidPrice("");
        ModelAndView modelAndView = new ModelAndView("bitstamp/add");
        modelAndView.addObject("paidPriceObject", balance);
        return modelAndView;
    }

    @GetMapping(value = {"/bitstamp/delete"})
    public ModelAndView showDeleteCryptoId() {
        List<Balance> deleteList = new ArrayList<Balance>();
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("/home/ywang/bitstamp/bitstampapi.properties"));
            Set<String> keys = props.stringPropertyNames();
            for (String key : keys) {
                if (key.endsWith(".paid.price")) {
                    System.out.println("key = " + key);
                    Balance balance = new Balance();
                    balance.setName(key);
                    System.out.println("paidPrice = " + props.get(key));
                    balance.setPaidPrice(props.getProperty(key));
                    deleteList.add(balance);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ModelAndView modelAndView = new ModelAndView("bitstamp/delete");
            modelAndView.addObject("deleteList", deleteList);
            return modelAndView;
        }
    }

    @GetMapping(value = {"/bitstamp/select/chart/{cryptoId}"})
    public ModelAndView selectChart(@PathVariable String cryptoId) {
        ModelAndView modelAndView = new ModelAndView("bitstamp/chartSelect");

        Map<String, Integer> minuteurList = new LinkedHashMap<String, Integer>();
        minuteurList.put("1 min", 60);
        minuteurList.put("3 min", 180);
        minuteurList.put("5 min", 300);
        minuteurList.put("15 min", 900);
        minuteurList.put("30 min", 1800);
        minuteurList.put("1 H", 3600);
        minuteurList.put("2 H", 7200);
        minuteurList.put("3 H", 14400);
        minuteurList.put("6 H", 21600);
        minuteurList.put("12 H", 43200);
        minuteurList.put("1 D", 86400);
        minuteurList.put("3 D", 259200);
        modelAndView.addObject("step", minuteurList);
        Balance balance = new Balance();
        balance.setName(cryptoId);
        balance.setPagination(60);;
        modelAndView.addObject("paidPriceObject", balance);
        return modelAndView;
    }

    @PostMapping(value = {"bitstamp/chart/{cryptoId}"})
    public ModelAndView buildCryptoChart(@PathVariable String cryptoId, @ModelAttribute("paidPriceObject") Balance balance, HttpServletResponse response) {
        System.out.println("name = " + cryptoId);
        System.out.println("pagination = " + balance.getPagination());
        try {
            String[] cryptoName = cryptoId.split("_");
            String newCryptoName = cryptoName[0] + "usd";
            String newCryptoNameWithUnderscore = cryptoName[0] + "_usd";
            BarSeries series = loadBarSeries(newCryptoName, balance.getPagination());
            OpenPriceIndicator openPrice = new OpenPriceIndicator(series);
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            String timeAfterConvertion = convertMinutesBy(balance.getPagination());
            dataset.addSeries(buildChartBarSeries(series, openPrice, newCryptoNameWithUnderscore + " [" + timeAfterConvertion + "] Open Price"));
            JFreeChart chart = ChartFactory.createTimeSeriesChart(newCryptoNameWithUnderscore + " [" + timeAfterConvertion + "] Open Price", // title
                    "Date", // x-axis label
                    "Price", // y-axis label
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

            response.setContentType("image/jpeg");
            OutputStream out = response.getOutputStream();
            writeChartAsJPEG(out , chart , width, height);

        } catch (IOException e) {
            e.printStackTrace();
        }
        ModelAndView modelAndView = new ModelAndView("bitstamp/chart");
        return  modelAndView;
    }

    private static org.jfree.data.time.TimeSeries buildChartBarSeries(BarSeries barSeries, Indicator<Num> indicator, String name) {
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            Bar bar = barSeries.getBar(i);
            //chartTimeSeries.add(new Hour(Date.from(bar.getEndTime().toInstant())), indicator.getValue(i).doubleValue());
            chartTimeSeries.add(new Minute(Date.from(bar.getEndTime().toInstant())), indicator.getValue(i).doubleValue());
        }
        return chartTimeSeries;
    }

    private BarSeries loadBarSeries(String barSeriesName, Integer step) {
        BarSeries series = new BaseBarSeriesBuilder().withName(barSeriesName).build();
        String composeUrl = "https://www.bitstamp.net/api/v2/ohlc/" + barSeriesName + "/?step=" + step + "&limit=1000";
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

    @PostMapping(value = {"/bitstamp/{cryptoId}/edit"})
    public String updateCryptoId(@PathVariable String cryptoId, @ModelAttribute("paidPriceObject") Balance balance) {
        try {
            FileInputStream in = new FileInputStream("/home/ywang/bitstamp/bitstampapi.properties");
            Properties props = new Properties();
            props.load(in);
            in.close();

            FileOutputStream out = new FileOutputStream("/home/ywang/bitstamp/bitstampapi.properties");
            String key = cryptoId.replace("_balance", "") + ".paid.price";
            props.setProperty(key, balance.getPaidPrice());
            props.store(out, null);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            return "redirect:/bitstamp";
        }
    }

    @PostMapping(value = {"/bitstamp/{cryptoId}/add"})
    public String addCryptoId(@PathVariable String cryptoId, @ModelAttribute("paidPriceObject") Balance balance) {
        try {
            FileInputStream in = new FileInputStream("/tmp/bitstamp/bitstampapi.properties");
            Properties props = new Properties();
            props.load(in);
            in.close();

            FileOutputStream out = new FileOutputStream("/tmp/bitstamp/bitstampapi.properties");
            String key = cryptoId.replace("_balance", "") + ".paid.price";
            props.setProperty(key, balance.getPaidPrice());
            props.store(out, null);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            return "redirect:/bitstamp";
        }
    }

    @GetMapping(value = {"/bitstamp/{cryptoId}/delete"})
    public String deleteCryptoId(@PathVariable String cryptoId) {
        try {
            FileInputStream in = new FileInputStream("/home/ywang/bitstamp/bitstampapi.properties");
            Properties props = new Properties();
            props.load(in);
            in.close();

            FileOutputStream out = new FileOutputStream("/home/ywang/bitstamp/bitstampapi.properties");
            props.remove(cryptoId);
            props.store(out, null);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            return "redirect:/bitstamp";
        }
    }

    @GetMapping(value = {"/bitstamp/pagination/{totalPagination}/{pageId}"})
    public ModelAndView getBalanceListByPagination(@PathVariable Integer totalPagination, @PathVariable Integer pageId) {
        ModelAndView modelAndView = new ModelAndView("bitstamp/singlePagination");
        List<Balance> balanceListByPagination = bitstampService.getBalanceListByPagination(totalPagination + 1 - pageId);
        List<Balance> balanceListByPaginationLatest = bitstampService.getBalanceListByPagination(totalPagination + 1);
        String datePagination = balanceListByPagination.get(0).getDate_pagination();
        modelAndView.addObject("balanceListByPagination", balanceListByPagination);
        modelAndView.addObject("balanceListByPaginationLatest", balanceListByPaginationLatest);
        modelAndView.addObject("datePagination", datePagination);
        modelAndView.addObject("pagination", totalPagination);
        modelAndView.addObject("current_pagination", pageId);
        return modelAndView;
    }

    private String getUserTransactions(Properties props) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException {
        String apiKey = String.format("%s %s", "BITSTAMP", props.getProperty("main.api.key"));
        String apiKeySecret = props.getProperty("main.api.secret");
        String httpVerb = "POST";
        String urlHost = "www.bitstamp.net";
        String urlPath = "/api/v2/user_transactions/";
        String urlQuery = "";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString();
        String contentType = "application/x-www-form-urlencoded";
        String version = "v2";
        String payloadString = "offset=0;limit=1000";
        String signature = apiKey +
                httpVerb +
                urlHost +
                urlPath +
                urlQuery +
                contentType +
                nonce +
                timestamp +
                version +
                payloadString;

        SecretKeySpec secretKey = new SecretKeySpec(apiKeySecret.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(signature.getBytes());
        signature = new String(Hex.encodeHex(rawHmac)).toUpperCase();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.bitstamp.net/api/v2/user_transactions/"))
                .POST(HttpRequest.BodyPublishers.ofString(payloadString))
                .setHeader("X-Auth", apiKey)
                .setHeader("X-Auth-Signature", signature)
                .setHeader("X-Auth-Nonce", nonce)
                .setHeader("X-Auth-Timestamp", timestamp)
                .setHeader("X-Auth-Version", version)
                .setHeader("Content-Type", contentType)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Status code not 200");
        }

        String serverSignature = response.headers().map().get("x-server-auth-signature").get(0);
        String responseContentType = response.headers().map().get("Content-Type").get(0);
        String stringToSign = nonce + timestamp + responseContentType + response.body();

        mac.init(secretKey);
        byte[] rawHmacServerCheck = mac.doFinal(stringToSign.getBytes());
        String newSignature = new String(Hex.encodeHex(rawHmacServerCheck));

        if (!newSignature.equals(serverSignature)) {
            throw new RuntimeException("Signatures do not match");
        }
        System.out.println("User_Transaction : " + response.body());
        return response.body();
    }

    private String getMainBalance(Properties props) {
        HttpResponse<String> response = null;

        try {
            String apiKey = String.format("%s %s", "BITSTAMP", props.getProperty("main.api.key"));
            String apiKeySecret = props.getProperty("main.api.secret");
            String httpVerb = "POST";
            String urlHost = "www.bitstamp.net";
            String urlPath = "/api/v2/balance/";
            String urlQuery = "";
            String timestamp = String.valueOf(System.currentTimeMillis());
            String nonce = UUID.randomUUID().toString();
            String contentType = "application/x-www-form-urlencoded";
            String version = "v2";
            String payloadString = "offset=1";
            String signature = apiKey +
                    httpVerb +
                    urlHost +
                    urlPath +
                    urlQuery +
                    contentType +
                    nonce +
                    timestamp +
                    version +
                    payloadString;

            SecretKeySpec secretKey = new SecretKeySpec(apiKeySecret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(signature.getBytes());
            signature = new String(Hex.encodeHex(rawHmac)).toUpperCase();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.bitstamp.net/api/v2/balance/"))
                    .POST(HttpRequest.BodyPublishers.ofString(payloadString))
                    .setHeader("X-Auth", apiKey)
                    .setHeader("X-Auth-Signature", signature)
                    .setHeader("X-Auth-Nonce", nonce)
                    .setHeader("X-Auth-Timestamp", timestamp)
                    .setHeader("X-Auth-Version", version)
                    .setHeader("Content-Type", contentType)
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Status code not 200");
            }

            String serverSignature = response.headers().map().get("x-server-auth-signature").get(0);
            String responseContentType = response.headers().map().get("Content-Type").get(0);
            String stringToSign = nonce + timestamp + responseContentType + response.body();

            mac.init(secretKey);
            byte[] rawHmacServerCheck = mac.doFinal(stringToSign.getBytes());
            String newSignature = new String(Hex.encodeHex(rawHmacServerCheck));

            if (!newSignature.equals(serverSignature)) {
                throw new RuntimeException("Signatures do not match");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println("balance :" + response.body());
        return response.body();
    }

    private String getProfitsBalance(Properties props) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException {
        String apiKey = String.format("%s %s", "BITSTAMP", props.getProperty("profits.api.key"));
        String apiKeySecret = props.getProperty("profits.api.secret");
        String httpVerb = "POST";
        String urlHost = "www.bitstamp.net";
        String urlPath = "/api/v2/balance/";
        String urlQuery = "";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString();
        String contentType = "application/x-www-form-urlencoded";
        String version = "v2";
        String payloadString = "offset=1";
        String signature = apiKey +
                httpVerb +
                urlHost +
                urlPath +
                urlQuery +
                contentType +
                nonce +
                timestamp +
                version +
                payloadString;

        SecretKeySpec secretKey = new SecretKeySpec(apiKeySecret.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(signature.getBytes());
        signature = new String(Hex.encodeHex(rawHmac)).toUpperCase();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.bitstamp.net/api/v2/balance/"))
                .POST(HttpRequest.BodyPublishers.ofString(payloadString))
                .setHeader("X-Auth", apiKey)
                .setHeader("X-Auth-Signature", signature)
                .setHeader("X-Auth-Nonce", nonce)
                .setHeader("X-Auth-Timestamp", timestamp)
                .setHeader("X-Auth-Version", version)
                .setHeader("Content-Type", contentType)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Status code not 200");
        }

        String serverSignature = response.headers().map().get("x-server-auth-signature").get(0);
        String responseContentType = response.headers().map().get("Content-Type").get(0);
        String stringToSign = nonce + timestamp + responseContentType + response.body();

        mac.init(secretKey);
        byte[] rawHmacServerCheck = mac.doFinal(stringToSign.getBytes());
        String newSignature = new String(Hex.encodeHex(rawHmacServerCheck));

        if (!newSignature.equals(serverSignature)) {
            throw new RuntimeException("Signatures do not match");
        }
        return response.body();
    }

    private Ticker getTicker(Balance balance) throws IOException {
        // Create a neat value object to hold the URL
        String crypto = balance.getName().replace("_balance", "");
        String composeUrl = "https://www.bitstamp.net/api/v2/ticker/" + crypto + "usd/";
        URL url = new URL(composeUrl);

        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        connection.setRequestProperty("accept", "application/json");

        // This line makes the request
        InputStream responseStream = connection.getInputStream();

        // Manually converting the response body InputStream to CurrentPrice using Jackson
        ObjectMapper mapper = new ObjectMapper();
        Ticker ticker = mapper.readValue(responseStream, Ticker.class);

        // Finally we have the response
        return ticker;

    }

    private Ticker getTicker(String crypto) throws IOException {
        // Create a neat value object to hold the URL
        String composeUrl = "https://www.bitstamp.net/api/v2/ticker/" + crypto + "/";
        URL url = new URL(composeUrl);

        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        connection.setRequestProperty("accept", "application/json");

        // This line makes the request
        InputStream responseStream = connection.getInputStream();

        // Manually converting the response body InputStream to CurrentPrice using Jackson
        ObjectMapper mapper = new ObjectMapper();
        Ticker ticker = mapper.readValue(responseStream, Ticker.class);

        // Finally we have the response
        return ticker;

    }

    private String convertMinutesBy(Integer step) {
        String result = "";
        switch(step) {
            case 60 :
                result = "1 min";
                break;
            case 180 :
                result = "3 min";
                break;
            case 300 :
                result = "5 min";
                break;
            case 900 :
                result = "15 min";
                break;
            case 1800 :
                result = "30 min";
                break;
            case 3600 :
                result = "1 hour";
                break;
            case 7200 :
                result = "2 hours";
                break;
            case 14400 :
                result = "3 hours";
                break;
            case 21600 :
                result = "6 hours";
                break;
            case 43200 :
                result = "12 hours";
                break;
            case 86400 :
                result = "1 day";
                break;
            case 259200 :
                result = "3 days";
                break;
            default:
                result = "1 min";
        }
        return result;
    }

    private String getPaidPrice(Properties props, String name) {
        String result;
        String crypto = name.replace("_balance", "");
        String keySecondPart = ".paid.price";
        if (props.getProperty(crypto+keySecondPart) !=  null) {
            result = props.getProperty(crypto+keySecondPart);
        } else {
            result = "-";
        }
        return result;
    }

    private String computeAmountSpendToBuy(String quantity, String paidPrice) {
        String result;
        if (paidPrice.equals("-")) {
            result = "-";
        } else if (paidPrice.contains(",")) {
            String[] tab = paidPrice.split(",");
            Double[] tabD = new Double[tab.length];
            for (int i = 0; i < tab.length; i++) {
                tabD[i] = Double.parseDouble(tab[i]);
            }

            double maxPaidPriceD = tabD[0];
            for (int counter = 1; counter < tabD.length; counter++) {
                if (tabD[counter] > maxPaidPriceD) {
                    maxPaidPriceD = tabD[counter];
                }
            }
            double quantityD = Double.parseDouble(quantity);

            double temp = quantityD * maxPaidPriceD;
            DecimalFormat df = new DecimalFormat("0.00");
            result = df.format(temp);
        } else {
            double paidPriceD = Double.parseDouble(paidPrice);
            double quantityD = Double.parseDouble(quantity);

            double temp = quantityD * paidPriceD;
            DecimalFormat df = new DecimalFormat("0.00");
            result = df.format(temp);
        }
        return result;
    }

    private String computeProfitOrLossAsValue(String paidPrice, String currentPrice, String quantity) {
        String result;
        double quantityD = Double.parseDouble(quantity);
        double totalMaxPaidPriceQuantity, totalCurrentPriceQuantity;
        if (paidPrice.equals("-")) {
            result = "-";
        } else if (paidPrice.contains(",")) {
            String[] tab = paidPrice.split(",");
            Double[] tabD = new Double[tab.length];
            for (int i = 0; i < tab.length; i++) {
                tabD[i] = Double.parseDouble(tab[i]);
            }

            double maxPaidPriceD = tabD[0];
            for (int counter = 1; counter < tabD.length; counter++) {
                if (tabD[counter] > maxPaidPriceD) {
                    maxPaidPriceD = tabD[counter];
                }
            }

            double currentPriceD = Double.parseDouble(currentPrice);
            totalMaxPaidPriceQuantity = maxPaidPriceD * quantityD;
            totalCurrentPriceQuantity = currentPriceD * quantityD;

            double temp = totalCurrentPriceQuantity - totalMaxPaidPriceQuantity;
            DecimalFormat df = new DecimalFormat("0.00");
            result = df.format(temp);
            if (!result.startsWith("-")) {
                result = "+" + result;
            }
        } else {
            double currentPriceD = Double.parseDouble(currentPrice);
            double paidPriceD = Double.parseDouble(paidPrice);
            totalMaxPaidPriceQuantity = paidPriceD * quantityD;
            totalCurrentPriceQuantity = currentPriceD * quantityD;

            double temp = totalCurrentPriceQuantity - totalMaxPaidPriceQuantity;
            DecimalFormat df = new DecimalFormat("0.00");
            result = df.format(temp);
            if (!result.startsWith("-")) {
                result = "+" + result;
            }
        }
        return result;
    }

    private String computeProfitOrLossCurrentPrice(String currentPrice, String paidPrice) {
        String result;
        if (paidPrice.equals("-")) {
            result = "black";
        } else if (paidPrice.contains(",")) {
            String[] tab = paidPrice.split(",");
            Double[] tabD = new Double[tab.length];
            for (int i = 0; i < tab.length; i++) {
                tabD[i] = Double.parseDouble(tab[i]);
            }

            double maxPaidPriceD = tabD[0];
            for (int counter = 1; counter < tabD.length; counter++) {
                if (tabD[counter] > maxPaidPriceD) {
                    maxPaidPriceD = tabD[counter];
                }
            }

            double currentPriceD = Double.parseDouble(currentPrice);
            if (maxPaidPriceD > currentPriceD) {
                result = "red";
            } else if (maxPaidPriceD == currentPriceD) {
                result = "black";
            } else {
                result = "green";
            }
        } else {
            double currentPriceD = Double.parseDouble(currentPrice);
            double paidPriceD = Double.parseDouble(paidPrice);
            if (paidPriceD > currentPriceD) {
                result = "red";
            } else if (paidPriceD == currentPriceD) {
                result = "black";
            } else {
                result = "green";
            }
        }

        return result;
    }

    private String computeUpDownPercentage(String paidPrice, String currentPrice) {
        String result = "-";
        if (!paidPrice.equals("-") && !currentPrice.equals("-")) {
            if (paidPrice.contains(",")) {
                String[] tab = paidPrice.split(",");
                Double[] tabD = new Double[tab.length];
                for (int i = 0; i < tab.length; i++) {
                    tabD[i] = Double.parseDouble(tab[i]);
                }

                double maxPaidPriceD = tabD[0];
                for (int counter = 1; counter < tabD.length; counter++) {
                    if (tabD[counter] > maxPaidPriceD) {
                        maxPaidPriceD = tabD[counter];
                    }
                }
                paidPrice = String.valueOf(maxPaidPriceD);
            }

            double paidPriceDouble = Double.parseDouble(paidPrice);
            double currentPriceDouble = Double.parseDouble(currentPrice);
            double temp = (currentPriceDouble * 100 / paidPriceDouble) - 100;
            DecimalFormat df = new DecimalFormat("0.00");
            result = df.format(temp);
            if (!result.startsWith("-")) {
                result = "+" + result;
            }
        }
        return result;
    }

    private List<BalanceForIA> copyBalanceListIntoBalanceForIAList(List<Balance> balanceList) {
        List<BalanceForIA> balanceForIAList = new ArrayList<BalanceForIA>();
        for (Balance balance : balanceList) {
            BalanceForIA balanceForIA = new BalanceForIA();
            balanceForIA.setName(balance.getName());
            balanceForIA.setValue(balance.getValue());
            balanceForIA.setPaidPrice(balance.getPaidPrice());
            balanceForIA.setCurrentPrice(balance.getCurrentPrice());
            balanceForIA.setAmountSpendToBuy(balance.getAmountSpendToBuy());
            balanceForIA.setProfitOrLossValue(balance.getProfitOrLossValue());
            balanceForIA.setProfitOrLossPercentage(balance.getProfitOrLossPercentage());
            balanceForIA.setPriceUpDownPercent(balance.getPriceUpDownPercent());
            balanceForIA.setPortfolioName(balance.getPortfolioName());
            balanceForIA.setLast24High(balance.getLast24High());
            balanceForIA.setLast24Low(balance.getLast24Low());
            balanceForIA.setLast24Volume(balance.getLast24Volume());
            Integer paginationForIA = bitstampService.getPaginationForIA();
            balanceForIA.setPagination(paginationForIA);
            balanceForIA.setDate_pagination(balance.getDate_pagination());

            balanceForIAList.add(balanceForIA);
        }
        return balanceForIAList;
    }

    public List<Balance> getBalanceList(String result) {
        List<Balance> balanceList = new ArrayList<Balance>();

        String subResult = result.substring(1, result.length()-1);
        String str[] = subResult.split(",");
        List<String> arrayList = new ArrayList<String>();
        arrayList = Arrays.asList(str);

        for (String s : arrayList) {
            String tab[] = s.split(":");
            String name = tab[0].substring(1, tab[0].length()-1);
            String value = tab[1].substring(1, tab[1].length()-1);
            if (name.contains("_balance")) {
                if (!value.equals("0.00000000") && !value.equals("0.00000") && !value.equals("0.00") && !value.equals("0")) {
                    Balance balance = new Balance();
                    balance.setName(name);
                    balance.setValue(value);
                    balanceList.add(balance);
                }
            }
        }
        return balanceList;
    }

    public List<Balance> getBalanceList2(List<Balance> balanceList) throws IOException {
        List<Balance> balanceList2 = new ArrayList<Balance>();
        for (Balance balance : balanceList) {
            if (!balance.getName().contains("usd") && !balance.getName().contains("eur")) {
                Balance balance2 = new Balance();
                balance2.setName(balance.getName());
                balance2.setValue(balance.getValue());
                Ticker ticker = getTicker(balance);
                balance2.setCurrentPrice(ticker.getLast());
                balance2.setLast24Low(ticker.getLow());
                balance2.setLast24High(ticker.getHigh());
                balance2.setLast24Volume(ticker.getVolume());
                balanceList2.add(balance2);
            } else {
                balance.setCurrentPrice("-");
                balance.setLast24Low("-");
                balance.setLast24High("-");
                balance.setLast24Volume("-");
                balanceList2.add(balance);
            }
        }
        return balanceList2;
    }

    public List<Balance> getBalanceList3(List<Balance> balanceList2, Properties props) {
        List<Balance> balanceList3 = new ArrayList<Balance>();
        for (Balance balance : balanceList2) {
            Balance balance3 = new Balance();
            balance3.setName(balance.getName());
            balance3.setValue(balance.getValue());
            balance3.setCurrentPrice(balance.getCurrentPrice());
            balance3.setLast24Low(balance.getLast24Low());
            balance3.setLast24High(balance.getLast24High());
            balance3.setLast24Volume(balance.getLast24Volume());
            balance3.setPaidPrice(getPaidPrice(props, balance.getName()));
            balanceList3.add(balance3);
        }
        return balanceList3;
    }

    public List<Balance> getBalanceList4(List<Balance> balanceList3, Properties props) throws NoSuchAlgorithmException, IOException, InvalidKeyException, InterruptedException {
        List<Balance> balanceList4 = new ArrayList<Balance>();
        for (Balance balance : balanceList3) {
            Balance balance4 = new Balance();
            balance4.setName(balance.getName());
            balance4.setValue(balance.getValue());
            balance4.setCurrentPrice(balance.getCurrentPrice());
            balance4.setLast24Low(balance.getLast24Low());
            balance4.setLast24High(balance.getLast24High());
            balance4.setLast24Volume(balance.getLast24Volume());
            balance4.setPaidPrice(balance.getPaidPrice());
            balance4.setAmountSpendToBuy(computeAmountSpendToBuy(balance.getValue(), balance.getPaidPrice()));
            balance4.setProfitOrLossValue(computeProfitOrLossAsValue(balance.getPaidPrice(), balance.getCurrentPrice(), balance.getValue()));
            balance4.setProfitOrLossPercentage(computeProfitOrLossCurrentPrice(balance.getCurrentPrice(), balance.getPaidPrice()));
            balance4.setPriceUpDownPercent(computeUpDownPercentage(balance.getPaidPrice(), balance.getCurrentPrice()));
            balance4.setPortfolioName("Main");
            balanceList4.add(balance4);
        }
        String result2 = getProfitsBalance(props).replace(" ", "");
        String subResult2 = result2.substring(1, result2.length()-1);
        String str2[] = subResult2.split(",");
        List<String> arrayList2 = new ArrayList<String>();
        arrayList2 = Arrays.asList(str2);
        for (String s : arrayList2) {
            String tab[] = s.split(":");
            String name = tab[0].substring(1, tab[0].length()-1);
            String value = tab[1].substring(1, tab[1].length()-1);
            if (name.equals("eur_balance") || name.equals("usd_balance")) {
                Balance balance = new Balance();
                balance.setName(name);
                balance.setValue(value);
                balance.setPaidPrice("-");
                balance.setCurrentPrice("-");
                balance.setLast24Low("-");
                balance.setLast24High("-");
                balance.setLast24Volume("-");
                balance.setAmountSpendToBuy("-");
                balance.setProfitOrLossValue("-");
                balance.setProfitOrLossPercentage("-");
                balance.setPriceUpDownPercent("-");
                balance.setPortfolioName("Profits");
                balanceList4.add(balance);
            }
        }

        return balanceList4;
    }

    public SuperThreeLists getSuperThreeLists(List<Balance> balanceList4) {
        return null;
    }
}
