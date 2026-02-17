package com.example.usermangment.controller;

import com.example.usermangment.dto.SolrServerDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/solr")
public class SolrController {

    @GetMapping("/servers")
    public List<SolrServerDto> getServers() {

        SolrServerDto s1 = new SolrServerDto();
        s1.setName("Solr-Node-1");
        s1.setHost("localhost");
        s1.setPort(8983);
        s1.setStatus("UP");

        SolrServerDto s2 = new SolrServerDto();
        s2.setName("Solr-Node-2");
        s2.setHost("localhost");
        s2.setPort(8984);
        s2.setStatus("DOWN");

        SolrServerDto s3 = new SolrServerDto();
        s3.setName("Solr-Node-3");
        s3.setHost("192.168.1.20");
        s3.setPort(8983);
        s3.setStatus("UP");

        return List.of(s1, s2, s3);
    }
}
