package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class})
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();
        assertEquals(6, genres.size());
        assertEquals(1, genres.get(0).getId());
        assertEquals("Комедия", genres.get(0).getName());
    }

    @Test
    void shouldFindGenreById() {
        Optional<Genre> genre = genreStorage.findById(1);
        assertTrue(genre.isPresent());
        assertEquals("Комедия", genre.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenGenreNotFound() {
        Optional<Genre> genre = genreStorage.findById(999);
        assertFalse(genre.isPresent());
    }
}