package com.library.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.backend.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
