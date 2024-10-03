package com.example.codecool.controller;


import com.example.codecool.dto.trigger.TriggerDto;
import com.example.codecool.service.trigger.TriggerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "trigger")
public class TriggerController {

    private final TriggerService triggerService;

    private static final Logger logger = LogManager.getLogger(TriggerController.class);

    @Autowired
    public TriggerController(TriggerService triggerService) {
        this.triggerService = triggerService;
    }


    @GetMapping
    public TriggerDto getAllJsons() {
        logger.info("Initiating request to process incoming JSON data.");
        TriggerDto response = triggerService.trigger();
        logger.info("Completed request to process incoming JSON data.");
        return response;
    }
}
