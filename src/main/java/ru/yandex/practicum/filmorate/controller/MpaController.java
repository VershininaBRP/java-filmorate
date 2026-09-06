package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;
import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaRatingStorage mpaRatingStorage;

    @GetMapping
    public List<MpaRating> findAll() {
        return mpaRatingStorage.findAll();
    }

    @GetMapping("/{id}")
    public MpaRating findById(@PathVariable int id) {
        return mpaRatingStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + id + " не найден"));
    }
}