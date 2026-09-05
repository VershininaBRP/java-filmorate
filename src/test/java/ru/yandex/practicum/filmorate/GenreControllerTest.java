package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@ContextConfiguration(classes = {GenreDbStorage.class, GenreService.class, GenreController.class})
public class GenreControllerTest {
    @Autowired
    private GenreController genreController;

    @Test
    public void testGetAllGenres() {
        List<Genre> genres = genreController.getAllGenres();
        assertThat(genres).hasSize(6);
        assertThat(genres.get(0).getId()).isEqualTo(1);
        assertThat(genres.get(0).getName()).isEqualTo("Комедия");
        assertThat(genres.get(1).getName()).isEqualTo("Драма");
        assertThat(genres.get(2).getName()).isEqualTo("Мультфильм");
        assertThat(genres.get(3).getName()).isEqualTo("Триллер");
        assertThat(genres.get(4).getName()).isEqualTo("Документальный");
        assertThat(genres.get(5).getName()).isEqualTo("Боевик");
    }

    @Test
    public void testGetGenreById() {
        Genre genre = genreController.getGenreById(1);
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isEqualTo("Комедия");

        Genre genre3 = genreController.getGenreById(3);
        assertThat(genre3.getId()).isEqualTo(3);
        assertThat(genre3.getName()).isEqualTo("Мультфильм");
    }

    @Test
    public void testGetGenreByIdNotFound() {
        assertThrows(NotFoundException.class, () -> genreController.getGenreById(999));
    }
}