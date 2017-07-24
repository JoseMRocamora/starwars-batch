package com.starwars.batch.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.batch.domain.Planet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class RestPlanetItemReader implements ItemReader<Planet> {
    public static final String PLANETS_URL = "http://swapi.co/api/planets/?format=json";

    private RestTemplate restTemplate= new RestTemplate();
    private ObjectMapper mapper = new ObjectMapper();


    private List<Planet> planets;


    @Override
    public Planet read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Planet planet = null;

        this.getPlanets();

        if (!planets.isEmpty()) {
            planet = planets.get(0);
            planets.remove(0);
        }
        else
            planets = null;

        return planet;
    }

    private void getPlanets() throws IOException {
        if (this.planets == null) {
            this.planets = new ArrayList<Planet>();
            JsonNode pagina = callSWApi(PLANETS_URL);
            int numPlanets = pagina.get("count").asInt();
            String nextUrl = pagina.get("next").asText();
            jsonNode2Planets(pagina);

            while (!"null".equalsIgnoreCase(nextUrl)) {
                log.info("Cargados {}/{}", planets.size(), numPlanets);
                pagina = callSWApi(nextUrl);
                jsonNode2Planets(pagina);
                nextUrl = pagina.get("next").asText();
            }
            log.info("Cargados {}/{}", planets.size(), numPlanets);
        }
    }

    private void jsonNode2Planets(JsonNode jsonNode) {
        JsonNode results = jsonNode.get("results");

        for(JsonNode nodo : results) {
            Planet planet = null;
            try {
                planet = this.mapper.treeToValue(nodo, Planet.class);
                planets.add(planet);
                //log.info("Planeta: " + nodo.toString());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
    }

    private JsonNode callSWApi(String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return this.mapper.readTree(response.getBody());
    }

}
