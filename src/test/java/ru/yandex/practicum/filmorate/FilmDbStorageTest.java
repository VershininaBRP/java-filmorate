package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class, GenreDbStorage.class, MpaRatingDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        MpaRating mpa = new MpaRating();
        mpa.setId(3);
        mpa.setName("PG-13");
        film.setMpaRating(mpa);
        Set<Genre> genres = new HashSet<>();
        Genre genre1 = new Genre();
        genre1.setId(1);
        genre1.setName("Комедия");
        Genre genre2 = new Genre();
        genre2.setId(2);
        genre2.setName("Драма");
        genres.add(genre1);
        genres.add(genre2);
        film.setGenres(genres);
        return film;
    }

    private User createValidUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void shouldCreateFilm() {
        Film film = createValidFilm();
        Film created = filmStorage.create(film);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("Тестовый фильм", created.getName());
        assertNotNull(created.getGenres());
        assertEquals(2, created.getGenres().size());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = createValidFilm();
        Film created = filmStorage.create(film);
        created.setName("Обновленный фильм");
        Film updated = filmStorage.update(created);
        assertEquals("Обновленный фильм", updated.getName());
    }

    @Test
    void shouldFindFilmById() {
        Film film = createValidFilm();
        Film created = filmStorage.create(film);
        Optional<Film> found = filmStorage.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(created.getName(), found.get().getName());
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.create(createValidFilm());
        filmStorage.create(createValidFilm());
        List<Film> films = filmStorage.findAll();
        assertEquals(2, films.size());
    }

    @Test
    void shouldDeleteFilm() {
        Film film = createValidFilm();
        Film created = filmStorage.create(film);
        filmStorage.delete(created.getId());
        Optional<Film> found = filmStorage.findById(created.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void shouldCheckExistsById() {
        Film film = createValidFilm();
        Film created = filmStorage.create(film);
        assertTrue(filmStorage.existsById(created.getId()));
        assertFalse(filmStorage.existsById(999));
    }

    @Test
    void shouldFindPopularFilms() {
        User user1 = userStorage.create(createValidUser("user1@test.com", "user1"));
        User user2 = userStorage.create(createValidUser("user2@test.com", "user2"));

        Film film1 = createValidFilm();
        film1.setName("Фильм 1");
        Film created1 = filmStorage.create(film1);

        Film film2 = createValidFilm();
        film2.setName("Фильм 2");
        Film created2 = filmStorage.create(film2);

        filmStorage.addLike(created1.getId(), user1.getId());
        filmStorage.addLike(created1.getId(), user2.getId());
        filmStorage.addLike(created2.getId(), user1.getId());

        List<Film> popular = filmStorage.findPopular(2);
        assertEquals(2, popular.size());
        assertEquals(created1.getId(), popular.get(0).getId());
        assertEquals(created2.getId(), popular.get(1).getId());
    }
}