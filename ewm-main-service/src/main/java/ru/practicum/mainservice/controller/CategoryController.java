package ru.practicum.mainservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.model.dto.CategoryDto;
import ru.practicum.mainservice.model.dto.NewCategoryDto;
import ru.practicum.mainservice.service.CategoryService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
public class CategoryController {

    public final CategoryService service;

    @Autowired
    public CategoryController(CategoryService service) {
        this.service = service;
    }

    //Публичный слой

    @GetMapping("/categories")
    public List<CategoryDto> findAll(@RequestParam(name = "from", defaultValue = "0") Integer from,
                                     @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get request to find all categories from={}, size={}", from, size);
        return service.findAll(from, size);
    }

    @GetMapping("/categories/{id}")
    public CategoryDto getById(@PathVariable("id") Integer id) {
        log.info("Get request to find category with id={}", id);
        return service.getById(id);
    }

    //Админский слой

    @PatchMapping("/admin/categories")
    public CategoryDto update(@RequestBody @Valid CategoryDto dto) {
        log.info("Get request to update category: request={}", dto);
        return service.update(dto);
    }

    @PostMapping("/admin/categories")
    public CategoryDto create(@RequestBody @Valid NewCategoryDto dto) {
        log.info("Get request to create category: request={}", dto);
        return service.create(dto);
    }

    @DeleteMapping("/admin/categories/{id}")
    public void deleteCategory(@PathVariable("id") Integer id) {
        log.warn("Get request to delete category with id={}", id);
        service.delete(id);
    }
}
