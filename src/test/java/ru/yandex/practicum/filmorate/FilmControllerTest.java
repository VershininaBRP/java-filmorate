package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.time.LocalDate;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        MpaRating mpa = new MpaRating();
        mpa.setId(3);
        mpa.setName("PG-13");
        film.setMpaRating(mpa);
        Genre genre1 = new Genre();
        genre1.setId(2);
        genre1.setName("Драма");
        Genre genre2 = new Genre();
        genre2.setId(1);
        genre2.setName("Комедия");
        film.setGenres(Set.of(genre1, genre2));
        return film;
    }

    @Test
    void shouldThrowExceptionWhenFilmNameIsEmpty() {
        Film film = createValidFilm();
        film.setName("");
        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsLongerThan200() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));
        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldCreateFilmWhenDescriptionIs200Characters() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getId() > 0);
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeMinimumDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldCreateFilmWithMinimumReleaseDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        Film film = createValidFilm();
        film.setDuration(0);
        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenMpaRatingIsNull() {
        Film film = createValidFilm();
        film.setMpaRating(null);
        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldCreateFilmWithGenres() {
        Film film = createValidFilm();
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Film created = response.getBody();
        assertNotNull(created);
        assertNotNull(created.getGenres());
        assertEquals(2, created.getGenres().size());
    }

    @Test
    void shouldCreateFilmWithoutGenres() {
        Film film = createValidFilm();
        film.setGenres(null);
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Film created = response.getBody();
        assertNotNull(created);
        assertNull(created.getGenres());
    }
}