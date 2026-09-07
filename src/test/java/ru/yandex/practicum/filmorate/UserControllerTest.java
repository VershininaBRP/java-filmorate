package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

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
        ResponseEntity<String> response = restTemplate.postForEntity("/users", user, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAt() {
        User user = createValidUser();
        user.setEmail("incorrect-email");
        ResponseEntity<String> response = restTemplate.postForEntity("/users", user, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenLoginIsEmpty() {
        User user = createValidUser();
        user.setLogin("");
        ResponseEntity<String> response = restTemplate.postForEntity("/users", user, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpace() {
        User user = createValidUser();
        user.setLogin("ivan ivanov");
        ResponseEntity<String> response = restTemplate.postForEntity("/users", user, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        User user = createValidUser();
        user.setLogin("ivan");
        user.setName("");
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ivan", response.getBody().getName());
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        ResponseEntity<String> response = restTemplate.postForEntity("/users", user, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldCreateUserWhenBirthdayIsToday() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldAddAndGetFriend() {
        User user1 = createValidUser();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        ResponseEntity<User> response1 = restTemplate.postForEntity("/users", user1, User.class);
        User created1 = response1.getBody();
        assertNotNull(created1);

        User user2 = createValidUser();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        ResponseEntity<User> response2 = restTemplate.postForEntity("/users", user2, User.class);
        User created2 = response2.getBody();
        assertNotNull(created2);

        restTemplate.put("/users/" + created1.getId() + "/friends/" + created2.getId(), null);

        ResponseEntity<User[]> friendsResponse = restTemplate.getForEntity(
                "/users/" + created1.getId() + "/friends", User[].class);
        assertEquals(HttpStatus.OK, friendsResponse.getStatusCode());
        User[] friends = friendsResponse.getBody();
        assertNotNull(friends);
        assertEquals(1, friends.length);
        assertEquals(created2.getId(), friends[0].getId());
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = createValidUser();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        ResponseEntity<User> response1 = restTemplate.postForEntity("/users", user1, User.class);
        User created1 = response1.getBody();
        assertNotNull(created1);

        User user2 = createValidUser();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        ResponseEntity<User> response2 = restTemplate.postForEntity("/users", user2, User.class);
        User created2 = response2.getBody();
        assertNotNull(created2);

        restTemplate.put("/users/" + created1.getId() + "/friends/" + created2.getId(), null);
        restTemplate.delete("/users/" + created1.getId() + "/friends/" + created2.getId());

        ResponseEntity<User[]> friendsResponse = restTemplate.getForEntity(
                "/users/" + created1.getId() + "/friends", User[].class);
        assertEquals(HttpStatus.OK, friendsResponse.getStatusCode());
        User[] friends = friendsResponse.getBody();
        assertNotNull(friends);
        assertEquals(0, friends.length);
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = createValidUser();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        ResponseEntity<User> response1 = restTemplate.postForEntity("/users", user1, User.class);
        User created1 = response1.getBody();
        assertNotNull(created1);

        User user2 = createValidUser();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        ResponseEntity<User> response2 = restTemplate.postForEntity("/users", user2, User.class);
        User created2 = response2.getBody();
        assertNotNull(created2);

        User user3 = createValidUser();
        user3.setEmail("user3@test.com");
        user3.setLogin("user3");
        ResponseEntity<User> response3 = restTemplate.postForEntity("/users", user3, User.class);
        User created3 = response3.getBody();
        assertNotNull(created3);

        restTemplate.put("/users/" + created1.getId() + "/friends/" + created2.getId(), null);
        restTemplate.put("/users/" + created1.getId() + "/friends/" + created3.getId(), null);
        restTemplate.put("/users/" + created2.getId() + "/friends/" + created3.getId(), null);

        ResponseEntity<User[]> commonResponse = restTemplate.getForEntity(
                "/users/" + created1.getId() + "/friends/common/" + created2.getId(), User[].class);
        assertEquals(HttpStatus.OK, commonResponse.getStatusCode());
        User[] common = commonResponse.getBody();
        assertNotNull(common);
        assertEquals(1, common.length);
        assertEquals(created3.getId(), common[0].getId());
    }
}