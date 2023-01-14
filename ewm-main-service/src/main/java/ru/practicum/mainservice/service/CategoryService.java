package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.mapper.UniversalMapper;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.model.dto.CategoryDto;
import ru.practicum.mainservice.model.dto.NewCategoryDto;
import ru.practicum.mainservice.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepo;
    private final UniversalMapper mapper;

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll(Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Category> result = categoryRepo.findAll(page).getContent();

        log.info("Found: {}", result.size());
        return mapper.toCategoryDtoList(result);
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Integer id) {
        return mapper.toCategoryDto(findCategory(id));
    }

    @Transactional
    public CategoryDto update(CategoryDto newCategory) {
        Category category = findCategory(newCategory.getId());
        category.setName(newCategory.getName());
        log.info("Category updated successfully.");
        return mapper.toCategoryDto(category);
    }

    @Transactional
    public CategoryDto create(NewCategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        Category result = categoryRepo.save(category);
        log.info("Category created successfully: id={}, name={}", result.getId(), result.getName());
        return mapper.toCategoryDto(result);
    }

    @Transactional
    public void delete(Integer id) {
        categoryRepo.deleteById(id);
        log.info("Category id=" + id + " deleted successfully.");
    }

    private Category findCategory(Integer id) {
        Optional<Category> category = categoryRepo.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException("Category with id=" + id + " not found.");
        } else {
            log.debug("Find category with id={}", id);
            return category.get();
        }
    }
}
