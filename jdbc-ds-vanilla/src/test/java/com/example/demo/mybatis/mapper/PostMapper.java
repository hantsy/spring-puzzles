package com.example.demo.mybatis.mapper;

import com.example.demo.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostMapper {

    // The mapping design is shit
//    @Results({
//            @Result(property = "body", column = "BODY")
//    })
    @Select("select * from POSTS")
    List<Post> findAll();
}
