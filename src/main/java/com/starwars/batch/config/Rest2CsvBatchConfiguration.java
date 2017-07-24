package com.starwars.batch.config;


import com.starwars.batch.domain.People;
import com.starwars.batch.domain.Planet;
import com.starwars.batch.listener.PeopleListener;
import com.starwars.batch.listener.PlanetListener;
import com.starwars.batch.reader.RestPlanetItemReader;
import com.starwars.batch.repository.PeopleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Writer;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableScheduling
public class Rest2CsvBatchConfiguration {

    @Bean
    public ItemReader<Planet> planetReader() {
        return new RestPlanetItemReader();
    }

/*
<bean id="cvsFileItemWriter" class="org.springframework.batch.item.file.FlatFileItemWriter">
	<!-- write to this csv file -->
	<property name="resource" value="file:cvs/report.csv" />
	<property name="shouldDeleteIfExists" value="true" />

	<property name="lineAggregator">
	  <bean
		class="org.springframework.batch.item.file.transform.DelimitedLineAggregator">
		<property name="delimiter" value="," />
		<property name="fieldExtractor">
		  <bean
			class="org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor">
			<property name="names" value="refId, name, age, csvDob, income" />
		   </bean>
		</property>
	   </bean>
	</property>
  </bean>
*/
    @Bean
    public ItemWriter<Planet> planetWriter() {
        FlatFileItemWriter<Planet> itemWriter = new FlatFileItemWriter<>();
        itemWriter.setResource(new FileSystemResource("src/main/resources/planets.csv"));
        itemWriter.setShouldDeleteIfExists(true);

        DelimitedLineAggregator<Planet> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(";");

        BeanWrapperFieldExtractor<Planet> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"name","rotationPeriod","orbitalPeriod","diameter","climate","gravity","terrain","surfaceWater","population"});

        lineAggregator.setFieldExtractor(fieldExtractor);

        itemWriter.setLineAggregator(lineAggregator);

        itemWriter.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("name;rotationPeriod;orbitalPeriod;diameter;climate;gravity;terrain;surfaceWater;population");
            }
        });

        return itemWriter;
    }

    @Bean
    public Step rest2CsvStep(StepBuilderFactory stepBuilderFactory, ItemReader planetReader, ItemWriter planetWriter, PlanetListener planetListener) {
        return stepBuilderFactory
                .get("rest2CsvStep")
                .chunk(10)
                .listener(planetListener)
                .reader(planetReader)
                .writer(planetWriter)
                .build();
    }

    @Bean
    public Job rest2CsvJob(JobBuilderFactory jobBuilderFactory, Step rest2CsvStep) {
        return jobBuilderFactory
                .get("rest2CsvJob")
                .incrementer(new RunIdIncrementer())
                .start(rest2CsvStep)
                .build();
    }
}
