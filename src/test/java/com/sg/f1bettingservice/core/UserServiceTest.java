package com.sg.f1bettingservice.core;

import static com.sg.f1bettingservice.core.UserService.INITIAL_BALANCE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sg.f1bettingservice.core.model.User;
import com.sg.f1bettingservice.persistence.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UserServiceTest {

  UserRepository userRepository = mock(UserRepository.class);
  UserService userService = new UserService(userRepository);

  @Test
  void shouldCreateUserFromRepositoryWhenItDoesNotExistTest() {
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    var user = User.builder().userId(1).balance(INITIAL_BALANCE).build();

    when(userRepository.findById(user.getUserId())).thenReturn(Optional.empty());
    when(userRepository.save(userCaptor.capture())).thenReturn(user);

    var result = userService.getOrCreate(user.getUserId());
    assertThat(result).isEqualTo(user);

    var createdUser = userCaptor.getValue();
    verify(userRepository).findById(user.getUserId());
    verify(userRepository).save(createdUser);
    assertThat(createdUser.getUserId()).isEqualTo(user.getUserId());
    assertThat(createdUser.getBalance()).isEqualTo(INITIAL_BALANCE);
  }

  @Test
  void shouldFetchExistingUserFromRepositoryWhenItExistsTest() {
    var user = User.builder().userId(1).balance(INITIAL_BALANCE).build();

    when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

    var result = userService.getOrCreate(user.getUserId());
    assertThat(result).isEqualTo(user);

    verify(userRepository).findById(user.getUserId());
    verify(userRepository, never()).save(user);
  }

  @Test
  void shouldSaveUserTest() {
    var user = User.builder().userId(1).balance(INITIAL_BALANCE).build();

    userService.save(user);
    verify(userRepository).save(user);
  }

  @Test
  void shouldSubtractAmountFromUserBalanceTest() {
    var userArgumentCaptor = ArgumentCaptor.forClass(User.class);
    var amountToSubtract = TEN;
    var user = User.builder().userId(1).balance(INITIAL_BALANCE).build();
    var expectedBalance = INITIAL_BALANCE.subtract(amountToSubtract);

    when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

    userService.subtractFromUserBalance(user.getUserId(), amountToSubtract);

    verify(userRepository).save(userArgumentCaptor.capture());
    assertThat(userArgumentCaptor.getValue().getBalance()).isEqualTo(expectedBalance);
  }

  @Test
  void shouldAddAmountToUserBalanceTest() {
    var userArgumentCaptor = ArgumentCaptor.forClass(User.class);
    var amountToAdd = TEN;
    var user = User.builder().userId(1).balance(INITIAL_BALANCE).build();
    var expectedBalance = INITIAL_BALANCE.add(amountToAdd);

    when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

    userService.addToUserBalance(user.getUserId(), amountToAdd);

    verify(userRepository).save(userArgumentCaptor.capture());
    assertThat(userArgumentCaptor.getValue().getBalance()).isEqualTo(expectedBalance);
  }
}
