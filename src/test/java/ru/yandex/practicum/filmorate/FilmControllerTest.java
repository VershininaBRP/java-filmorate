package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void beforeEach() {
        filmController = new FilmController();
    }

    @Test
    void shouldThrowExceptionWhenFilmNameIsEmpty() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsLongerThan200() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldCreateFilmWhenDescriptionIs200Characters() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm);
        assertEquals(1, createdFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeMinimumDate() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldCreateFilmWithMinimumReleaseDate() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);
        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm);
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }
}