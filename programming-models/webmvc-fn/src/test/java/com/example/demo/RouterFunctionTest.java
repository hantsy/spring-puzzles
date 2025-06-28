package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class RouterFunctionTest {

    @TestConfiguration
    @Import({WebConfig.class, PostHandler.class})
    static class TestConfig {
    }

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RouterFunction<ServerResponse> routerFunction;

    @MockitoBean
    PostRepository posts;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.routerFunctions(routerFunction).build();
    }


    @Test
    public void getAll() throws Exception {
        when(posts.findAll()).thenReturn(
                Stream.of(
                        new Post(1L, "test one", "content one", LocalDateTime.now()),
                        new Post(2L, "test two", "content two", LocalDateTime.now())
                )
        );

        mockMvc.perform(get("/posts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2));

        verify(posts, times(1)).findAll();
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void getPostById() throws Exception {
        var id = 1L;
        var post = new Post(id, "test one", "content one", LocalDateTime.now());
        var idCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.findById(idCaptor.capture())).thenReturn(Optional.of(post));

        mockMvc.perform(get("/posts/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id));

        assertThat(idCaptor.getValue()).isEqualTo(id);

        verify(posts, times(1)).findById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void getPostById_nonExisting() throws Exception {
        var id = 1L;
        when(posts.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/posts/{id}", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(posts, times(1)).findById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void createPost() throws Exception {
        var id = 1L;
        when(posts.create(any(Post.class))).thenReturn(id);
        var data = new Post(null, "title one", "content one", null);
        mockMvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON).content(asJsonString(data)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(header().string("Location", CoreMatchers.containsString("/posts/" + id)));

        verify(posts, times(1)).create(any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void updatePost() throws Exception {
        var id = 1L;
        var idCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.update(idCaptor.capture(), any(Post.class))).thenReturn(1);

        var data = new Post(null, "updated test one", " updated content one", LocalDateTime.now());

        mockMvc.perform(put("/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).content(asJsonString(data)))
                .andExpect(status().isNoContent());

        assertThat(idCaptor.getValue()).isEqualTo(id);

        verify(posts, times(1)).update(anyLong(), any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void updatePost_nonExisting() throws Exception {
        var id = 1L;
        when(posts.update(anyLong(), any(Post.class))).thenReturn(0);

        var data = new Post(null, "updated test one", " updated content one", LocalDateTime.now());
        mockMvc.perform(put("/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).content(asJsonString(data)))
                .andExpect(status().isNotFound());

        verify(posts, times(1)).update(anyLong(), any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void deleteById() throws Exception {
        var id = 1L;

        var deletedIdCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.deleteById(deletedIdCaptor.capture())).thenReturn(1);

        mockMvc.perform(delete("/posts/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(deletedIdCaptor.getValue()).isEqualTo(id);

        verify(posts, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void deleteById_nonExisting() throws Exception {
        var id = 1L;
        when(posts.deleteById(anyLong())).thenReturn(0);

        mockMvc.perform(delete("/posts/{id}", id))
                .andExpect(status().isNotFound());

        verify(posts, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
