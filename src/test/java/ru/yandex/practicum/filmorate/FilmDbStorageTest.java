package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@ContextConfiguration(classes = {FilmDbStorage.class, UserDbStorage.class, GenreDbStorage.class, MpaDbStorage.class, FriendHelper.class, LikeHelper.class})
public class FilmDbStorageTest {
    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Autowired
    private LikeHelper likeHelper;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Film createTestFilm() {
        MpaRating mpa = mpaDbStorage.findById(1).orElseThrow();
        Film film = new Film();
        film.setName("Test Film" + counter.getAndIncrement());
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpaRating(mpa);
        return film;
    }

    private User createTestUser() {
        User user = new User();
        int num = counter.getAndIncrement();
        user.setEmail("test" + num + "@test.com");
        user.setLogin("testLogin" + num);
        user.setName("Test User" + num);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    @Test
    public void testCreateAndFindFilm() {
        MpaRating mpa = mpaDbStorage.findById(1).orElseThrow();
        Genre genre = genreDbStorage.findById(1).orElseThrow();

        Film film = createTestFilm();
        film.setMpaRating(mpa);
        List<Genre> genres = new ArrayList<>();
        genres.add(genre);
        film.setGenres(genres);

        Film created = filmDbStorage.create(film);
        assertThat(created.getId()).isPositive();

        Optional<Film> found = filmDbStorage.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(film.getName());
        assertThat(found.get().getGenres()).hasSize(1);
    }

    @Test
    public void testAddAndRemoveLike() {
        User createdUser = userDbStorage.create(createTestUser());
        Film film = createTestFilm();
        Film createdFilm = filmDbStorage.create(film);

        likeHelper.addLike(createdFilm.getId(), createdUser.getId());

        Optional<Film> filmWithLike = filmDbStorage.findById(createdFilm.getId());
        assertThat(filmWithLike).isPresent();
        assertThat(filmWithLike.get().getLikes()).contains(createdUser.getId());

        likeHelper.removeLike(createdFilm.getId(), createdUser.getId());

        Optional<Film> filmWithoutLike = filmDbStorage.findById(createdFilm.getId());
        assertThat(filmWithoutLike).isPresent();
        assertThat(filmWithoutLike.get().getLikes()).doesNotContain(createdUser.getId());
    }

    @Test
    public void testUpdateFilm() {
        Film film = createTestFilm();
        Film created = filmDbStorage.create(film);

        created.setName("Updated Name");
        created.setDescription("Updated Description");
        filmDbStorage.update(created);

        Optional<Film> found = filmDbStorage.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Name");
        assertThat(found.get().getDescription()).isEqualTo("Updated Description");
    }

    @Test
    public void testDeleteFilm() {
        Film film = createTestFilm();
        Film created = filmDbStorage.create(film);
        filmDbStorage.delete(created.getId());
        Optional<Film> found = filmDbStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindAllFilms() {
        filmDbStorage.create(createTestFilm());
        filmDbStorage.create(createTestFilm());
        assertThat(filmDbStorage.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    public void testExistsById() {
        Film film = createTestFilm();
        Film created = filmDbStorage.create(film);
        assertThat(filmDbStorage.existsById(created.getId())).isTrue();
        assertThat(filmDbStorage.existsById(999)).isFalse();
    }

    @Test
    public void testFindPopularFilms() {
        User createdUser = userDbStorage.create(createTestUser());

        Film film1 = createTestFilm();
        film1.setName("Popular Film");
        Film createdFilm1 = filmDbStorage.create(film1);
        likeHelper.addLike(createdFilm1.getId(), createdUser.getId());

        Film film2 = createTestFilm();
        film2.setName("Not Popular Film");
        filmDbStorage.create(film2);

        assertThat(likeHelper.findPopular(10))
                .isNotEmpty()
                .first()
                .satisfies(film -> assertThat(film.getName()).isEqualTo("Popular Film"));
    }
}