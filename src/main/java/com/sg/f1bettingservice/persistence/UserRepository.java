package com.sg.f1bettingservice.persistence;

import com.sg.f1bettingservice.core.model.User;
import java.util.Optional;

public interface UserRepository {

  Optional<User> findById(Integer userId);

  User save(User user);
}
