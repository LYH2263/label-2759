package com.library.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.backend.entity.Book;
import com.library.backend.entity.BorrowRecord;
import com.library.backend.mapper.BookMapper;
import com.library.backend.mapper.BorrowRecordMapper;
import com.library.backend.mapper.UserMapper;
import com.library.backend.service.BorrowRecordService;
import com.library.backend.utils.Result;
import com.library.backend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> borrowBook(Long userId, Long bookId) {
        // 1. Check book stock
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            return Result.error("图书不存在");
        }
        if (book.getStock() <= 0) {
            return Result.error("图书库存不足");
        }

        // 2. Check if user has already borrowed this book and not returned
        Long count = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getBookId, bookId)
                .isNull(BorrowRecord::getReturnDate));
        
        if (count > 0) {
            return Result.error("您已借阅该图书且未归还");
        }

        // 3. Create borrow record
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDateTime.now());
        // Default due date is 30 days later
        record.setDueDate(LocalDateTime.now().plusDays(30));
        record.setStatus(0); // 0: Borrowed, 1: Returned
        
        borrowRecordMapper.insert(record);

        // 4. Decrease stock
        book.setStock(book.getStock() - 1);
        bookMapper.updateById(book);

        return Result.success("借阅成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> returnBook(Long id) {
        BorrowRecord record = borrowRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("借阅记录不存在");
        }
        
        // Ownership check
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null && !record.getUserId().equals(currentUserId)) {
            // Check if admin
            List<String> roles = userMapper.getRoleCodesByUserId(currentUserId);
            if (!roles.contains("ROLE_ADMIN")) {
                return Result.error("无权操作他人的借阅记录");
            }
        }

        if (record.getReturnDate() != null) {
            return Result.error("该图书已归还");
        }

        // 1. Update record
        record.setReturnDate(LocalDateTime.now());
        record.setStatus(1); // Returned
        borrowRecordMapper.updateById(record);

        // 2. Increase stock
        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            book.setStock(book.getStock() + 1);
            bookMapper.updateById(book);
        }

        return Result.success("归还成功");
    }

    @Override
    public IPage<BorrowRecord> getMyBorrowRecords(Page<BorrowRecord> page, Long userId) {
        return borrowRecordMapper.selectPageWithDetail(page, new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .orderByDesc(BorrowRecord::getBorrowDate));
    }

    @Override
    public IPage<BorrowRecord> getAllBorrowRecords(Page<BorrowRecord> page, String username) {
        QueryWrapper<BorrowRecord> wrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like("u.username", username);
        }
        wrapper.orderByDesc("br.borrow_date");
        return borrowRecordMapper.selectPageWithDetail(page, wrapper);
    }
}
