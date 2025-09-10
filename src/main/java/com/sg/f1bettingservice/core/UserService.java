package com.sg.f1bettingservice.core;

import com.sg.f1bettingservice.core.model.User;
import com.sg.f1bettingservice.persistence.UserRepository;
import java.math.BigDecimal;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

  static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(100);

  private final UserRepository userRepository;

  public User getOrCreate(Integer userId) {
    var existing = userRepository.findById(userId);
    return existing.orElseGet(() -> createUserWithIdAndInitialBalance(userId));
  }

  public void save(User user) {
    userRepository.save(user);
  }

  public void subtractFromUserBalance(Integer userId, BigDecimal amount) {
    updateUserBalance(userId, amount, BigDecimal::subtract);
  }

  public void addToUserBalance(Integer userId, BigDecimal amount) {
    updateUserBalance(userId, amount, BigDecimal::add);
  }

  private void updateUserBalance(
      Integer userId, BigDecimal amount, BiFunction<BigDecimal, BigDecimal, BigDecimal> operation) {
    userRepository
        .findById(userId)
        .map(
            user -> {
              user.setBalance(operation.apply(user.getBalance(), amount));
              return user;
            })
        .map(userRepository::save);
  }

  private User createUserWithIdAndInitialBalance(Integer userId) {
    var user = User.builder().userId(userId).balance(INITIAL_BALANCE).build();
    return userRepository.save(user);
  }
}
