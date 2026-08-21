package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void beforeEach() {
        userController = new UserController();
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        User user = createValidUser();
        user.setEmail("");

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAt() {
        User user = createValidUser();
        user.setEmail("incorrect-email");

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }

    @Test
    void shouldThrowExceptionWhenLoginIsEmpty() {
        User user = createValidUser();
        user.setLogin("");

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpace() {
        User user = createValidUser();
        user.setLogin("ivan ivanov");

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        User user = createValidUser();
        user.setLogin("ivan");
        user.setName("");
        User createdUser = userController.create(user);

        assertEquals("ivan", createdUser.getName());
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }

    @Test
    void shouldCreateUserWhenBirthdayIsToday() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());
        User createdUser = userController.create(user);

        assertNotNull(createdUser);
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("testLogin");
        user.setName("Test");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}