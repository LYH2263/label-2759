package com.library.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.backend.annotation.RequireRole;
import com.library.backend.entity.BorrowRecord;
import com.library.backend.service.BorrowRecordService;
import com.library.backend.utils.Result;
import com.library.backend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow")
public class BorrowRecordController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    @PostMapping("/{bookId}")
    public Result<String> borrowBook(@PathVariable Long bookId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return borrowRecordService.borrowBook(userId, bookId);
    }

    @PostMapping("/return/{id}")
    public Result<String> returnBook(@PathVariable Long id) {
        return borrowRecordService.returnBook(id);
    }

    @GetMapping("/my")
    public Result<IPage<BorrowRecord>> getMyBorrowRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(borrowRecordService.getMyBorrowRecords(new Page<>(page, size), userId));
    }

    @GetMapping("/list")
    @RequireRole("ROLE_ADMIN")
    public Result<IPage<BorrowRecord>> getAllBorrowRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username) {
        return Result.success(borrowRecordService.getAllBorrowRecords(new Page<>(page, size), username));
    }
}
