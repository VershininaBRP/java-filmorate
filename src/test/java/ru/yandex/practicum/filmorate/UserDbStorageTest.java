package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class})
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void shouldCreateUser() {
        User user = createValidUser();
        User created = userStorage.create(user);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("test@test.com", created.getEmail());
        assertEquals("testLogin", created.getLogin());
    }

    @Test
    void shouldUpdateUser() {
        User user = createValidUser();
        User created = userStorage.create(user);
        created.setName("Updated Name");
        User updated = userStorage.update(created);
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void shouldFindUserById() {
        User user = createValidUser();
        User created = userStorage.create(user);
        Optional<User> found = userStorage.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(created.getEmail(), found.get().getEmail());
    }

    @Test
    void shouldFindAllUsers() {
        userStorage.create(createValidUser());
        userStorage.create(createValidUser());
        List<User> users = userStorage.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void shouldDeleteUser() {
        User user = createValidUser();
        User created = userStorage.create(user);
        userStorage.delete(created.getId());
        Optional<User> found = userStorage.findById(created.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void shouldCheckExistsById() {
        User user = createValidUser();
        User created = userStorage.create(user);
        assertTrue(userStorage.existsById(created.getId()));
        assertFalse(userStorage.existsById(999));
    }

    @Test
    void shouldAddAndGetFriend() {
        User user1 = createValidUser();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        User created1 = userStorage.create(user1);

        User user2 = createValidUser();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        User created2 = userStorage.create(user2);

        userStorage.addFriend(created1.getId(), created2.getId());

        List<User> friends = userStorage.getFriends(created1.getId());
        assertEquals(1, friends.size());
        assertEquals(created2.getId(), friends.get(0).getId());
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = createValidUser();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        User created1 = userStorage.create(user1);

        User user2 = createValidUser();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        User created2 = userStorage.create(user2);

        userStorage.addFriend(created1.getId(), created2.getId());
        userStorage.removeFriend(created1.getId(), created2.getId());

        List<User> friends = userStorage.getFriends(created1.getId());
        assertEquals(0, friends.size());
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = createValidUser();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        User created1 = userStorage.create(user1);

        User user2 = createValidUser();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        User created2 = userStorage.create(user2);

        User user3 = createValidUser();
        user3.setEmail("user3@test.com");
        user3.setLogin("user3");
        User created3 = userStorage.create(user3);

        userStorage.addFriend(created1.getId(), created2.getId());
        userStorage.addFriend(created1.getId(), created3.getId());
        userStorage.addFriend(created2.getId(), created3.getId());

        List<User> commonFriends = userStorage.getCommonFriends(created1.getId(), created2.getId());
        assertEquals(1, commonFriends.size());
        assertEquals(created3.getId(), commonFriends.get(0).getId());
    }
}