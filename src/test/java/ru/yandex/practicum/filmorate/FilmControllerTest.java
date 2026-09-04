package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;
    private FilmService filmService;

    @BeforeEach
    void beforeEach() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService);
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaRating(MpaRating.PG_13);
        film.setGenres(Set.of(Genre.DRAMA, Genre.COMEDY));
        return film;
    }

    @Test
    void shouldThrowExceptionWhenFilmNameIsEmpty() {
        Film film = createValidFilm();
        film.setName("");

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsLongerThan200() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldCreateFilmWhenDescriptionIs200Characters() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));
        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm);
        assertEquals(1, createdFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeMinimumDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
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

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldThrowExceptionWhenMpaRatingIsNull() {
        Film film = createValidFilm();
        film.setMpaRating(null);

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldCreateFilmWithGenres() {
        Film film = createValidFilm();
        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getGenres());
        assertEquals(2, createdFilm.getGenres().size());
        assertTrue(createdFilm.getGenres().contains(Genre.DRAMA));
        assertTrue(createdFilm.getGenres().contains(Genre.COMEDY));
    }

    @Test
    void shouldCreateFilmWithoutGenres() {
        Film film = createValidFilm();
        film.setGenres(null);
        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm);
        assertNull(createdFilm.getGenres());
    }
}