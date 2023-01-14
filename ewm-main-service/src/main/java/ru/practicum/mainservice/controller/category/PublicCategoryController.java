package ru.practicum.mainservice.controller.category;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.model.dto.CategoryDto;
import ru.practicum.mainservice.service.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@Validated
public class PublicCategoryController {

    public final CategoryService service;

    @Autowired
    public PublicCategoryController(CategoryService service) {
        this.service = service;
    }

    //Публичный слой

    @GetMapping("/categories")
    public List<CategoryDto> findAll(@RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                     @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Get request to find all categories from={}, size={}", from, size);
        return service.findAll(from, size);
    }

    @GetMapping("/categories/{id}")
    public CategoryDto getById(@PathVariable("id") Integer id) {
        log.info("Get request to find category with id={}", id);
        return service.getById(id);
    }

}
