package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.controller.MpaController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@ContextConfiguration(classes = {MpaDbStorage.class, MpaService.class, MpaController.class})
public class MpaControllerTest {
    @Autowired
    private MpaController mpaController;

    @Test
    public void testGetAllMpaRatings() {
        List<MpaRating> ratings = mpaController.getAllMpaRatings();
        assertThat(ratings).hasSize(5);
        assertThat(ratings.get(0).getId()).isEqualTo(1);
        assertThat(ratings.get(0).getName()).isEqualTo("G");
        assertThat(ratings.get(1).getName()).isEqualTo("PG");
        assertThat(ratings.get(2).getName()).isEqualTo("PG-13");
        assertThat(ratings.get(3).getName()).isEqualTo("R");
        assertThat(ratings.get(4).getName()).isEqualTo("NC-17");
    }

    @Test
    public void testGetMpaRatingById() {
        MpaRating rating = mpaController.getMpaRatingById(1);
        assertThat(rating.getId()).isEqualTo(1);
        assertThat(rating.getName()).isEqualTo("G");

        MpaRating rating3 = mpaController.getMpaRatingById(3);
        assertThat(rating3.getId()).isEqualTo(3);
        assertThat(rating3.getName()).isEqualTo("PG-13");
    }

    @Test
    public void testGetMpaRatingByIdNotFound() {
        assertThrows(NotFoundException.class, () -> mpaController.getMpaRatingById(999));
    }
}