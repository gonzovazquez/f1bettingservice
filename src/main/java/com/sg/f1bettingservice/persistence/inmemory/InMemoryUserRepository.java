package com.sg.f1bettingservice.persistence.inmemory;

import static java.util.Optional.ofNullable;

import com.sg.f1bettingservice.core.model.User;
import com.sg.f1bettingservice.persistence.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserRepository implements UserRepository {

  private final Map<Integer, User> users = new ConcurrentHashMap<>();

  @Override
  public Optional<User> findById(Integer userId) {
    return ofNullable(users.get(userId));
  }

  @Override
  public User save(User user) {
    users.put(user.getUserId(), user);
    return user;
  }
}
