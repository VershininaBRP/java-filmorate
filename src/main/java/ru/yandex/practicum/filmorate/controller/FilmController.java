package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final List<Film> films = new ArrayList<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private int nextId = 1;

    @PostMapping
    public Film create(@RequestBody Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.add(film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        validateFilm(film);
        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getId() == film.getId()) {
                films.set(i, film);
                log.info("Обновлён фильм: {}", film);
                return film;
            }
        }

        log.error("Ошибка обновления: фильм с id {} не найден", film.getId());
        throw new NotFoundException(
                "Фильм с id " + film.getId() + " не найден"
        );
    }

    @GetMapping
    public List<Film> findAll() {
        return films;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации фильма: название не может быть пустым");
            throw new ValidationException(
                    "Название фильма не может быть пустым"
            );
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error(
                    "Ошибка валидации фильма: описание превышает 200 символов"
            );
            throw new ValidationException(
                    "Описание фильма не может быть длиннее 200 символов"
            );
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error(
                    "Ошибка валидации фильма: дата релиза {} раньше {}",
                    film.getReleaseDate(),
                    MIN_RELEASE_DATE
            );
            throw new ValidationException(
                    "Дата релиза не может быть раньше 28 декабря 1895 года"
            );
        }

        if (film.getDuration() <= 0) {
            log.error(
                    "Ошибка валидации фильма: продолжительность {} должна быть положительной",
                    film.getDuration()
            );
            throw new ValidationException(
                    "Продолжительность фильма должна быть положительной"
            );
        }
    }
}