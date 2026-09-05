package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        User created = userService.createUser(user);
        log.info("Создан пользователь: {}", created);
        return created;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        User updated = userService.updateUser(user);
        log.info("Обновлён пользователь: {}", updated);
        return updated;
    }

    @GetMapping
    public List<User> findAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable int id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable int id, @PathVariable int friendId) {
        User user = userService.addFriend(id, friendId);
        log.info("Пользователь {} отправил заявку в друзья пользователю {}", id, friendId);
        return user;
    }

    @PutMapping("/{id}/friends/{friendId}/accept")
    public User acceptFriend(@PathVariable int id, @PathVariable int friendId) {
        User user = userService.acceptFriend(id, friendId);
        log.info("Пользователь {} принял заявку от {}", id, friendId);
        return user;
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable int id, @PathVariable int friendId) {
        User user = userService.removeFriend(id, friendId);
        log.info("Пользователь {} удалил из друзей {}", id, friendId);
        return user;
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/pending")
    public List<User> getPendingRequests(@PathVariable int id) {
        return userService.getPendingRequests(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}