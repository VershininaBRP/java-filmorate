package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@ContextConfiguration(classes = {FilmDbStorage.class, UserDbStorage.class, GenreDbStorage.class, MpaDbStorage.class, FilmService.class, UserService.class, FilmController.class, FriendHelper.class, LikeHelper.class})
public class FilmControllerTest {
    @Autowired
    private FilmController filmController;

    @Autowired
    private UserDbStorage userDbStorage;

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Autowired
    private GenreDbStorage genreDbStorage;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        MpaRating mpa = mpaDbStorage.findById(3).orElseThrow();
        film.setMpaRating(mpa);
        List<Genre> genres = new ArrayList<>();
        genreDbStorage.findById(2).ifPresent(genres::add);
        genreDbStorage.findById(1).ifPresent(genres::add);
        genres.sort((g1, g2) -> Integer.compare(g1.getId(), g2.getId()));
        film.setGenres(genres);
        return film;
    }

    private User createTestUser() {
        User user = new User();
        int num = counter.getAndIncrement();
        user.setEmail("user" + num + "@test.com");
        user.setLogin("login" + num);
        user.setName("User" + num);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    @Test
    void shouldThrowExceptionWhenFilmNameIsEmpty() {
        Film film = createValidFilm();
        film.setName("");
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsLongerThan200() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldCreateFilmWhenDescriptionIs200Characters() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));
        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm);
        assertTrue(createdFilm.getId() > 0);
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeMinimumDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldCreateFilmWithMinimumReleaseDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm);
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        Film film = createValidFilm();
        film.setDuration(0);
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenMpaRatingIsNull() {
        Film film = createValidFilm();
        film.setMpaRating(null);
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldCreateFilmWithGenres() {
        Film film = createValidFilm();
        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getGenres());
        assertEquals(2, createdFilm.getGenres().size());
    }

    @Test
    void shouldCreateFilmWithoutGenres() {
        Film film = createValidFilm();
        film.setGenres(null);
        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm);
        assertTrue(createdFilm.getGenres() == null || createdFilm.getGenres().isEmpty());
    }

    @Test
    void shouldFindFilmById() {
        Film film = createValidFilm();
        Film created = filmController.create(film);
        Film found = filmController.findById(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getName(), found.getName());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = createValidFilm();
        Film created = filmController.create(film);
        created.setName("Обновленный фильм");
        Film updated = filmController.update(created);
        assertEquals("Обновленный фильм", updated.getName());
    }

    @Test
    void shouldAddLike() {
        Film film = createValidFilm();
        Film createdFilm = filmController.create(film);
        User createdUser = userDbStorage.create(createTestUser());
        Film filmWithLike = filmController.addLike(createdFilm.getId(), createdUser.getId());
        assertNotNull(filmWithLike);
        assertTrue(filmWithLike.getLikes().contains(createdUser.getId()));
    }

    @Test
    void shouldRemoveLike() {
        Film film = createValidFilm();
        Film createdFilm = filmController.create(film);
        User createdUser = userDbStorage.create(createTestUser());
        filmController.addLike(createdFilm.getId(), createdUser.getId());
        Film filmWithoutLike = filmController.removeLike(createdFilm.getId(), createdUser.getId());
        assertFalse(filmWithoutLike.getLikes().contains(createdUser.getId()));
    }
}