package com.library.backend.controller;

import com.library.backend.service.BookService;
import com.library.backend.service.BorrowRecordService;
import com.library.backend.service.UserService;
import com.library.backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowRecordService borrowRecordService;

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("bookCount", bookService.count());
        stats.put("userCount", userService.count());
        stats.put("borrowCount", borrowRecordService.count());
        // Simple overdue count logic could be added here if service supports it
        // For now, basic counts are enough for the dashboard
        return Result.success(stats);
    }
}
