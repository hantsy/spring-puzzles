package com.example.demo.mybatis.mapper;

import com.example.demo.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostMapper {

    @Select("select * from POSTS")
    List<Post> findAll();

    // The mapping design is shit
    @Results({
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "title", column = "TITLE")
    })
    @Select("select * from POSTS")
    List<PostSummary> findSummaries();
}
