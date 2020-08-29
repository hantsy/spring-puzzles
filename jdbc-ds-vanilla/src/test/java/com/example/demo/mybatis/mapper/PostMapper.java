package com.example.demo.mybatis.mapper;

import com.example.demo.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper {

//    @Results({
//            @Result(property = "body", column = "BODY")
//    })
    @Select("select * from POSTS")
    List<Post> findAll();
}
