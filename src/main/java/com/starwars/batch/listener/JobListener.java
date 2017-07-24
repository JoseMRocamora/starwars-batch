package com.starwars.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        //jobExecution.getStepExecutions().forEach(stepExecution -> {});
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

    }
}
