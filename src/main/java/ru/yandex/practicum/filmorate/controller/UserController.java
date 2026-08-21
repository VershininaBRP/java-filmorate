package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final List<User> users = new ArrayList<>();

    @PostMapping
    public User create(@RequestBody User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.add(user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == user.getId()) {
                users.set(i, user);
                log.info("Обновлён пользователь: {}", user);
                return user;
            }
        }

        log.error(
                "Ошибка обновления: пользователь с id {} не найден",
                user.getId()
        );
        throw new ValidationException(
                "Пользователь с id " + user.getId() + " не найден"
        );
    }

    @GetMapping
    public List<User> findAll() {
        return users;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error(
                    "Ошибка валидации пользователя: email не может быть пустым"
            );
            throw new ValidationException(
                    "Email не может быть пустым"
            );
        }

        if (!user.getEmail().contains("@")) {
            log.error(
                    "Ошибка валидации пользователя: email {} не содержит @",
                    user.getEmail()
            );
            throw new ValidationException(
                    "Email должен содержать символ @"
            );
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error(
                    "Ошибка валидации пользователя: логин не может быть пустым"
            );
            throw new ValidationException(
                    "Логин не может быть пустым"
            );
        }

        if (user.getLogin().contains(" ")) {
            log.error(
                    "Ошибка валидации пользователя: логин {} содержит пробел",
                    user.getLogin()
            );

            throw new ValidationException(
                    "Логин не должен содержать пробелы"
            );
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error(
                    "Ошибка валидации пользователя: дата рождения {} некорректна",
                    user.getBirthday()
            );
            throw new ValidationException(
                    "Дата рождения не может быть в будущем"
            );
        }
    }

    private int getNextId() {
        int currentMaxId = users.stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}