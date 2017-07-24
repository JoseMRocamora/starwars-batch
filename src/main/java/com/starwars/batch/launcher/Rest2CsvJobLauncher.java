package com.starwars.batch.launcher;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Rest2CsvJobLauncher {

    private JobLauncher jobLauncher;
    private Job rest2CsvJob;

    public Rest2CsvJobLauncher(JobLauncher jobLauncher, Job rest2CsvJob) {
        this.jobLauncher = jobLauncher;
        this.rest2CsvJob = rest2CsvJob;
    }

    @Scheduled(fixedDelay = 120000)
    public void run() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(rest2CsvJob, jobParameters);
    }
}
