package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendHelper;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendHelper friendHelper;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FriendHelper friendHelper) {
        this.userStorage = userStorage;
        this.friendHelper = friendHelper;
    }

    public User addFriend(int userId, int friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        if (friendHelper.areFriends(userId, friendId)) {
            throw new ValidationException("Пользователи уже дружат");
        }
        friendHelper.addFriendRequest(userId, friendId);
        return getUserOrThrow(userId);
    }

    public User acceptFriend(int userId, int friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        if (!friendHelper.hasPendingRequest(friendId, userId)) {
            throw new NotFoundException("Нет входящей заявки от пользователя " + friendId);
        }
        friendHelper.acceptFriendRequest(friendId, userId);
        return getUserOrThrow(userId);
    }

    public User removeFriend(int userId, int friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        if (!friendHelper.areFriends(userId, friendId)) {
            throw new NotFoundException("Пользователи не являются друзьями");
        }
        friendHelper.removeFriend(userId, friendId);
        return getUserOrThrow(userId);
    }

    public List<User> getFriends(int userId) {
        getUserOrThrow(userId);
        List<Integer> friendIds = friendHelper.getFriendIds(userId);
        return friendIds.stream()
                .map(this::getUserOrThrow)
                .toList();
    }

    public List<User> getPendingRequests(int userId) {
        getUserOrThrow(userId);
        List<Integer> requestIds = friendHelper.getPendingRequests(userId);
        return requestIds.stream()
                .map(this::getUserOrThrow)
                .toList();
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        getUserOrThrow(userId);
        getUserOrThrow(otherId);
        List<Integer> userFriends = friendHelper.getFriendIds(userId);
        List<Integer> otherFriends = friendHelper.getFriendIds(otherId);
        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::getUserOrThrow)
                .toList();
    }

    public User getUserById(int id) {
        return getUserOrThrow(id);
    }

    public List<User> getAllUsers() {
        return userStorage.findAll();
    }

    public User createUser(User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        if (!userStorage.existsById(user.getId())) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    private User getUserOrThrow(int id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}