package com.library.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.backend.entity.BorrowRecord;
import com.library.backend.utils.Result;

public interface BorrowRecordService extends IService<BorrowRecord> {
    Result<String> borrowBook(Long userId, Long bookId);
    Result<String> returnBook(Long id);
    IPage<BorrowRecord> getMyBorrowRecords(Page<BorrowRecord> page, Long userId);
    IPage<BorrowRecord> getAllBorrowRecords(Page<BorrowRecord> page, String username);
}
