package com.example.demo.mybatis;

import com.example.demo.Post;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MybatisPostRepository {
    private final SqlSessionTemplate template;

    public List<Post> findAll() {
       return  this.template.<Post>selectList(
                "com.example.demo.mybatis.mapper.PostMapper.findAll"// terrible literal statement.
        );
    }
}
