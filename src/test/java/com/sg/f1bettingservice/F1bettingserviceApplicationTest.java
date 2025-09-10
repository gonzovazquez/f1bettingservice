package com.sg.f1bettingservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class F1bettingserviceApplicationTest {

  @Autowired private RestTemplate restTemplate;

  @Test
  void contextLoads() {
    F1bettingserviceApplication.main(new String[] {});
  }

  @Test
  void restTemplateBeanShouldBeAvailable() {
    assertThat(restTemplate).isNotNull();
  }
}
