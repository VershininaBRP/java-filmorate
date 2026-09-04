package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private UserService userService;

    @BeforeEach
    void beforeEach() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userService);
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("testLogin");
        user.setName("Test");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
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

    @Test
    void shouldAddFriendWithPendingStatus() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());

        userController.addFriend(user1.getId(), user2.getId());

        assertEquals(FriendshipStatus.PENDING, user1.getFriends().get(user2.getId()));
        assertEquals(FriendshipStatus.PENDING, user2.getFriends().get(user1.getId()));
    }

    @Test
    void shouldConfirmFriendship() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());

        userController.addFriend(user1.getId(), user2.getId());
        userService.confirmFriend(user2.getId(), user1.getId());

        User updatedUser1 = userController.findById(user1.getId());
        User updatedUser2 = userController.findById(user2.getId());

        assertEquals(FriendshipStatus.CONFIRMED, updatedUser1.getFriends().get(user2.getId()));
        assertEquals(FriendshipStatus.CONFIRMED, updatedUser2.getFriends().get(user1.getId()));
    }

    @Test
    void shouldThrowExceptionWhenConfirmingNonExistentFriendship() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());

        assertThrows(
                NotFoundException.class,
                () -> userService.confirmFriend(user1.getId(), user2.getId())
        );
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());

        userController.addFriend(user1.getId(), user2.getId());
        userController.removeFriend(user1.getId(), user2.getId());

        User updatedUser1 = userController.findById(user1.getId());
        User updatedUser2 = userController.findById(user2.getId());

        assertFalse(updatedUser1.getFriends().containsKey(user2.getId()));
        assertFalse(updatedUser2.getFriends().containsKey(user1.getId()));
    }

    @Test
    void shouldGetFriends() {
        User user1 = userController.create(createValidUser());
        User user2 = userController.create(createValidUser());
        User user3 = userController.create(createValidUser());

        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3.getId());

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

        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user2.getId(), user1.getId());
        userController.addFriend(user1.getId(), user3.getId());
        userController.addFriend(user2.getId(), user3.getId());

        List<User> commonFriends = userController.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(user3.getId(), commonFriends.get(0).getId());
    }
}