package com.xiaoya.accounting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounting")
public class AccountingServiceController {
    @GetMapping("/health")
    public String health(){
        return "accounting-service is running";
    }
}
