package com.yan.wang.googlesheet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GoogleSheetController {

    @Autowired
    private GoogleSheetService googleSheetService;

    @GetMapping("/googlesheet")
    @ResponseStatus(value = HttpStatus.OK)
    public ModelAndView getGoogleSheetContent() {
        System.out.println("1");
        String content = googleSheetService.getGoogleSheetContent();
        ModelAndView modelAndView = new ModelAndView("googlesheet/gs");
        modelAndView.addObject("content", content);
        return modelAndView;
    }
}
