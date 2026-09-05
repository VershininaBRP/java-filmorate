package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendHelper;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@ContextConfiguration(classes = {UserDbStorage.class, FriendHelper.class})
public class UserDbStorageTest {
    @Autowired
    private UserDbStorage userDbStorage;

    @Autowired
    private FriendHelper friendHelper;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private User createTestUser() {
        User user = new User();
        int num = counter.getAndIncrement();
        user.setEmail("test" + num + "@test.com");
        user.setLogin("testLogin" + num);
        user.setName("Test User" + num);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    @Test
    public void testCreateAndFindUser() {
        User user = createTestUser();
        User created = userDbStorage.create(user);
        assertThat(created.getId()).isPositive();

        Optional<User> found = userDbStorage.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(found.get().getLogin()).isEqualTo(user.getLogin());
        assertThat(found.get().getName()).isEqualTo(user.getName());
    }

    @Test
    public void testAddAndAcceptFriendRequest() {
        User user1 = createTestUser();
        User createdUser1 = userDbStorage.create(user1);
        User user2 = createTestUser();
        User createdUser2 = userDbStorage.create(user2);

        friendHelper.addFriendRequest(createdUser1.getId(), createdUser2.getId());

        List<Integer> pendingRequests = friendHelper.getPendingRequests(createdUser2.getId());
        assertThat(pendingRequests).hasSize(1);
        assertThat(pendingRequests.get(0)).isEqualTo(createdUser1.getId());

        friendHelper.acceptFriendRequest(createdUser1.getId(), createdUser2.getId());

        List<Integer> friends1 = friendHelper.getFriendIds(createdUser1.getId());
        assertThat(friends1).hasSize(1);
        assertThat(friends1.get(0)).isEqualTo(createdUser2.getId());

        List<Integer> friends2 = friendHelper.getFriendIds(createdUser2.getId());
        assertThat(friends2).hasSize(1);
        assertThat(friends2.get(0)).isEqualTo(createdUser1.getId());

        assertTrue(friendHelper.areFriends(createdUser1.getId(), createdUser2.getId()));
        assertTrue(friendHelper.areFriends(createdUser2.getId(), createdUser1.getId()));
    }

    @Test
    public void testRemoveFriend() {
        User user1 = createTestUser();
        User createdUser1 = userDbStorage.create(user1);
        User user2 = createTestUser();
        User createdUser2 = userDbStorage.create(user2);

        friendHelper.addFriendRequest(createdUser1.getId(), createdUser2.getId());
        friendHelper.acceptFriendRequest(createdUser1.getId(), createdUser2.getId());

        assertTrue(friendHelper.areFriends(createdUser1.getId(), createdUser2.getId()));
        assertTrue(friendHelper.areFriends(createdUser2.getId(), createdUser1.getId()));

        friendHelper.removeFriend(createdUser1.getId(), createdUser2.getId());

        List<Integer> friendsAfterRemove1 = friendHelper.getFriendIds(createdUser1.getId());
        assertThat(friendsAfterRemove1).isEmpty();

        List<Integer> friendsAfterRemove2 = friendHelper.getFriendIds(createdUser2.getId());
        assertThat(friendsAfterRemove2).isEmpty();

        assertFalse(friendHelper.areFriends(createdUser1.getId(), createdUser2.getId()));
        assertFalse(friendHelper.areFriends(createdUser2.getId(), createdUser1.getId()));
    }

    @Test
    public void testFindCommonFriends() {
        User user1 = createTestUser();
        User createdUser1 = userDbStorage.create(user1);
        User user2 = createTestUser();
        User createdUser2 = userDbStorage.create(user2);
        User user3 = createTestUser();
        User createdUser3 = userDbStorage.create(user3);

        friendHelper.addFriendRequest(createdUser1.getId(), createdUser3.getId());
        friendHelper.acceptFriendRequest(createdUser1.getId(), createdUser3.getId());
        friendHelper.addFriendRequest(createdUser2.getId(), createdUser3.getId());
        friendHelper.acceptFriendRequest(createdUser2.getId(), createdUser3.getId());

        List<Integer> commonFriends = friendHelper.getFriendIds(createdUser1.getId()).stream()
                .filter(friendHelper.getFriendIds(createdUser2.getId())::contains)
                .toList();

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0)).isEqualTo(createdUser3.getId());
    }

    @Test
    public void testUpdateUser() {
        User user = createTestUser();
        User created = userDbStorage.create(user);

        created.setName("Updated Name");
        created.setEmail("updated" + counter.getAndIncrement() + "@test.com");
        userDbStorage.update(created);

        Optional<User> found = userDbStorage.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    public void testDeleteUser() {
        User user = createTestUser();
        User created = userDbStorage.create(user);
        userDbStorage.delete(created.getId());
        Optional<User> found = userDbStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindAllUsers() {
        userDbStorage.create(createTestUser());
        userDbStorage.create(createTestUser());
        assertThat(userDbStorage.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    public void testExistsById() {
        User user = createTestUser();
        User created = userDbStorage.create(user);
        assertThat(userDbStorage.existsById(created.getId())).isTrue();
        assertThat(userDbStorage.existsById(999)).isFalse();
    }
}