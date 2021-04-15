package com.yan.wang.findata;

import com.google.common.base.Throwables;
import com.yan.wang.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class BuySellBtcUsdController {

    @Autowired
    private BuySellBtcUsdService buySellBtcUsdService;

    @GetMapping("/findata")
    @ResponseStatus(value = HttpStatus.OK)
    public ModelAndView getListOfBuySellBtcUsd() {
        System.out.println("1");
        List<BuySellBtcUsd> buySellBtcUsdList = buySellBtcUsdService.getListOfBuySellBtcUsd();
        System.out.println("5");
        System.out.println(buySellBtcUsdList.size());
        System.out.println("6");
        ModelAndView modelAndView = new ModelAndView("findata/findata");
        modelAndView.addObject("buySellBtcUsdList", buySellBtcUsdList);
        return modelAndView;
    }
}
