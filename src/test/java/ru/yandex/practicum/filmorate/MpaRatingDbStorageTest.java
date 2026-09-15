package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaRatingDbStorage.class})
class MpaRatingDbStorageTest {

    @Autowired
    private MpaRatingDbStorage mpaRatingStorage;

    @Test
    void shouldFindAllMpaRatings() {
        List<MpaRating> ratings = mpaRatingStorage.findAll();
        assertEquals(5, ratings.size());
        assertEquals(1, ratings.get(0).getId());
        assertEquals("G", ratings.get(0).getName());
    }

    @Test
    void shouldFindMpaRatingById() {
        Optional<MpaRating> rating = mpaRatingStorage.findById(3);
        assertTrue(rating.isPresent());
        assertEquals("PG-13", rating.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenMpaRatingNotFound() {
        Optional<MpaRating> rating = mpaRatingStorage.findById(999);
        assertFalse(rating.isPresent());
    }
}