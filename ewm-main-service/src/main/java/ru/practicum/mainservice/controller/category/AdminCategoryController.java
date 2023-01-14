package ru.practicum.mainservice.controller.category;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.model.dto.CategoryDto;
import ru.practicum.mainservice.model.dto.NewCategoryDto;
import ru.practicum.mainservice.service.CategoryService;

import javax.validation.Valid;

@Slf4j
@RestController
public class AdminCategoryController {

    public final CategoryService service;

    @Autowired
    public AdminCategoryController(CategoryService service) {
        this.service = service;
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
    public void delete(@PathVariable("id") Integer id) {
        log.warn("Get request to delete category with id={}", id);
        service.delete(id);
    }
}
