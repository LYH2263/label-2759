package com.library.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.backend.annotation.RequireRole;
import com.library.backend.entity.Book;
import com.library.backend.service.BookService;
import com.library.backend.utils.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public Result<Page<Book>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category) {
        
        Page<Book> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(title)) {
            wrapper.like(Book::getTitle, title);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Book::getCategory, category);
        }
        
        wrapper.orderByDesc(Book::getCreateTime);
        
        return Result.success(bookService.page(pageParam, wrapper));
    }

    @GetMapping("/{id}")
    public Result<Book> getById(@PathVariable Long id) {
        return Result.success(bookService.getById(id));
    }

    @PostMapping
    @RequireRole("ROLE_ADMIN")
    public Result<Boolean> save(@Valid @RequestBody Book book) {
        return Result.success(bookService.save(book));
    }

    @PutMapping
    @RequireRole("ROLE_ADMIN")
    public Result<Boolean> update(@Valid @RequestBody Book book) {
        return Result.success(bookService.updateById(book));
    }

    @DeleteMapping("/{id}")
    @RequireRole("ROLE_ADMIN")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(bookService.removeById(id));
    }
}
