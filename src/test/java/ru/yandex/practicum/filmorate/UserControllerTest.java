package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FriendHelper;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@ContextConfiguration(classes = {UserDbStorage.class, UserService.class, UserController.class, FriendHelper.class})
public class UserControllerTest {
    @Autowired
    private UserController userController;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private User createValidUser() {
        User user = new User();
        int num = counter.getAndIncrement();
        user.setEmail("test" + num + "@test.com");
        user.setLogin("testLogin" + num);
        user.setName("Test" + num);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        User user = createValidUser();
        user.setEmail("");
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAt() {
        User user = createValidUser();
        user.setEmail("incorrect-email");
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsEmpty() {
        User user = createValidUser();
        user.setLogin("");
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpace() {
        User user = createValidUser();
        user.setLogin("ivan ivanov");
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        User user = createValidUser();
        user.setLogin("ivan" + counter.getAndIncrement());
        user.setName("");
        User createdUser = userController.create(user);
        assertEquals(user.getLogin(), createdUser.getName());
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldCreateUserWhenBirthdayIsToday() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());
        User createdUser = userController.create(user);
        assertNotNull(createdUser);
        assertTrue(createdUser.getId() > 0);
    }

    @Test
    void shouldSendFriendRequest() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());

        User updatedUser = userController.addFriend(user1.getId(), user2.getId());

        List<User> pendingRequests = userController.getPendingRequests(user2.getId());
        assertEquals(1, pendingRequests.size());
        assertEquals(user1.getId(), pendingRequests.get(0).getId());
    }

    @Test
    void shouldAcceptFriendRequest() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());

        userController.addFriend(user1.getId(), user2.getId());
        userController.acceptFriend(user2.getId(), user1.getId());

        List<User> friends1 = userController.getFriends(user1.getId());
        List<User> friends2 = userController.getFriends(user2.getId());

        assertTrue(friends1.stream().anyMatch(u -> u.getId() == user2.getId()));
        assertFalse(friends2.stream().anyMatch(u -> u.getId() == user1.getId()));
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());

        userController.addFriend(user1.getId(), user2.getId());
        userController.acceptFriend(user2.getId(), user1.getId());
        userController.removeFriend(user1.getId(), user2.getId());

        List<User> friends1 = userController.getFriends(user1.getId());
        List<User> friends2 = userController.getFriends(user2.getId());

        assertFalse(friends1.stream().anyMatch(u -> u.getId() == user2.getId()));
        assertFalse(friends2.stream().anyMatch(u -> u.getId() == user1.getId()));
    }

    @Test
    void shouldGetFriends() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());
        User user3 = userController.create(createValidUser());

        userController.addFriend(user1.getId(), user2.getId());
        userController.acceptFriend(user2.getId(), user1.getId());
        userController.addFriend(user1.getId(), user3.getId());
        userController.acceptFriend(user3.getId(), user1.getId());

        List<User> friends = userController.getFriends(user1.getId());
        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId() == user2.getId()));
        assertTrue(friends.stream().anyMatch(u -> u.getId() == user3.getId()));
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());
        User user3 = userController.create(createValidUser());

        userController.addFriend(user1.getId(), user3.getId());
        userController.acceptFriend(user3.getId(), user1.getId());
        userController.addFriend(user2.getId(), user3.getId());
        userController.acceptFriend(user3.getId(), user2.getId());

        List<User> commonFriends = userController.getCommonFriends(user1.getId(), user2.getId());
        assertEquals(1, commonFriends.size());
        assertEquals(user3.getId(), commonFriends.get(0).getId());
    }

    @Test
    void shouldFindUserById() {
        User user = createValidUser();
        User created = userController.create(user);
        User found = userController.findById(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getEmail(), found.getEmail());
        assertEquals(created.getLogin(), found.getLogin());
    }

    @Test
    void shouldUpdateUser() {
        User user = createValidUser();
        User created = userController.create(user);
        created.setName("Обновленное имя");
        User updated = userController.update(created);
        assertEquals("Обновленное имя", updated.getName());
    }

    @Test
    void shouldGetAllUsers() {
        userController.create(createValidUser());
        userController.create(createValidUser());
        List<User> users = userController.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> userController.findById(999));
    }

    @Test
    void shouldThrowNotFoundWhenAddingFriendToNonExistentUser() {
        User user = userController.create(createValidUser());
        assertThrows(NotFoundException.class, () -> userController.addFriend(999, user.getId()));
    }

    @Test
    void shouldThrowExceptionWhenAcceptingNonExistentRequest() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());
        assertThrows(NotFoundException.class, () -> userController.acceptFriend(user2.getId(), user1.getId()));
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentFriend() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());
        assertThrows(NotFoundException.class, () -> userController.removeFriend(user1.getId(), user2.getId()));
    }
}