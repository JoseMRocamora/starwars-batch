package com.starwars.batch.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PeopleListener {

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        log.info( stepExecution.getSummary() );
    }
}
