package com.example.demo.Service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.Converter.CategoryConverter;
import com.example.demo.DTO.CategoryDto;
import com.example.demo.DTO.CategoryGroupDto;
import com.example.demo.Entity.CategoryEntity;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Service.Impl.CategoryServiceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryConverter categoryConverter;
    private final CategoryEntity categoryEntity = CategoryEntity.builder().build();
    private final CategoryDto categoryDto = CategoryDto.builder().build();

    @Test
    void getCategory() {
        List<CategoryGroupDto> arrays = new ArrayList<>();
        arrays.add(CategoryGroupDto.builder().parent("parent").category(Arrays.asList("name1","name2")).build());
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(
                CategoryEntity.builder().parent("parent").name("name1").build(),
                CategoryEntity.builder().parent("parent").name("name2").build()));
        List<CategoryGroupDto> result = categoryService.getCategory();
        Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(arrays);
        Assertions.assertThat(result.get(0).getCategory()).contains("name1").contains("name2");
    }

    @Test
    void getCategoryByParent() {
        when(categoryRepository.findByParent(anyString())).thenReturn(
                Collections.singletonList(CategoryEntity.builder().build()));
        when(categoryConverter.toDto(anyList())).thenReturn(Collections.singletonList(categoryDto));

        List<CategoryDto> result = categoryService.getCategoryByParent("parent");
        Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(Collections.singletonList(categoryDto));
    }

    @Test
    void saveCategory() {
        when(categoryConverter.toEntity(any(CategoryDto.class))).thenReturn(categoryEntity);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(categoryEntity);
        when(categoryConverter.toDto(any(CategoryEntity.class))).thenReturn(categoryDto);

        CategoryDto result = categoryService.saveCategory(CategoryDto.builder().build());
        Assertions.assertThat(result).isEqualTo(categoryDto);
    }

    @Test
    void deleteCategory() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(categoryEntity));
        doNothing().when(categoryRepository).delete(any(CategoryEntity.class));

        Long result = categoryService.deleteCategory(1L);
        Assertions.assertThat(result).isEqualTo(1L);
        verify(categoryRepository, times(1)).delete(categoryEntity);
    }
}
