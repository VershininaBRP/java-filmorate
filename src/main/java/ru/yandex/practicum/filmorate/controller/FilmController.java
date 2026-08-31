package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        Film created = filmService.createFilm(film);
        log.info("Добавлен фильм: {}", created);
        return created;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        Film updated = filmService.updateFilm(film);
        log.info("Обновлён фильм: {}", updated);
        return updated;
    }

    @GetMapping
    public List<Film> findAll() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable int id, @PathVariable int userId) {
        Film film = filmService.addLike(id, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
        return film;
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable int id, @PathVariable int userId) {
        Film film = filmService.removeLike(id, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, id);
        return film;
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(required = false, defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }
}