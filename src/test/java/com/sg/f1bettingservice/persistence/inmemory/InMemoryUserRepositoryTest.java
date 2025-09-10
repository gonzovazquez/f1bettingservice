package com.sg.f1bettingservice.persistence.inmemory;

import static org.assertj.core.api.Assertions.assertThat;

import com.sg.f1bettingservice.core.model.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InMemoryUserRepositoryTest {

  InMemoryUserRepository userRepository = new InMemoryUserRepository();

  @Test
  void shouldCreateUserAndFindItById() {
    var user = User.builder().userId(1).balance(BigDecimal.TEN).build();
    var createdUser = userRepository.save(user);
    var existingUser = userRepository.findById(user.getUserId()).orElseThrow();
    assertThat(existingUser).isEqualTo(createdUser);
    assertThat(existingUser).isEqualTo(user);
  }
}
