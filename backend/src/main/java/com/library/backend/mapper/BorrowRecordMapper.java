package com.library.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.backend.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {
    
    @Select("SELECT br.*, b.title as book_title, u.username as username " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN sys_user u ON br.user_id = u.id " +
            " ${ew.customSqlSegment}")
    Page<BorrowRecord> selectPageWithDetail(Page<BorrowRecord> page, @Param(Constants.WRAPPER) Wrapper<BorrowRecord> wrapper);
}
