package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class Application {

    @Autowired
    private SampleProperties sampleProperties;

    @RequestMapping("/")
    public String index() {
      return sampleProperties.getIndex();
    }

    @RequestMapping("/secret")
    public String secret() {
      return sampleProperties.getSecret();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
