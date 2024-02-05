package com.example.demo.Service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.Converter.PostConverter;
import com.example.demo.DTO.PostDto;
import com.example.demo.DTO.PostLikeDto;
import com.example.demo.DTO.PostPageDto;
import com.example.demo.Entity.PostEntity;
import com.example.demo.Entity.PostLikeEntity;
import com.example.demo.Entity.UserEntity;
import com.example.demo.Repository.PostLikeRepository;
import com.example.demo.Repository.PostRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.Impl.PostServiceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostConverter postConverter;
    @InjectMocks
    private PostServiceImpl postService;
    private final Pageable pageable = PageRequest.of(0, 3, Direction.DESC, "pid");
    private final UserEntity userEntity = UserEntity.builder().uid(1L).provider("google_1").build();
    private final PostEntity postEntity = PostEntity.builder().pid(1L).uid(userEntity).build();
    private final PostPageDto postPageDto = PostPageDto.builder().build();
    private final PostDto postDto = PostDto.builder().pid(1L).build();


    @Test
    void findPostPage() {
        PostPageDto pageDto = PostPageDto.builder().build();
        when(postRepository.findAll(this.pageable)).thenReturn(new PageImpl<>(new ArrayList<>()));
        when(postConverter.toDto(ArgumentMatchers.<Page<PostEntity>>any())).thenReturn(pageDto);

        PostPageDto result = postService.findPostPage(this.pageable);
        Assertions.assertThat(result).isEqualTo(pageDto);
    }

    @Test
    void findPost() {
        when(postRepository.findByPid(any(Long.class))).thenReturn(Optional.of(postEntity));
        when(postConverter.toDto(any(PostEntity.class))).thenReturn(postDto);

        PostDto result = postService.findPost(1L);
        Assertions.assertThat(result).isEqualTo(postDto);

    }

    @Test
    void findPostByTitle() {
        when(postRepository.findByTitleContaining(anyString(), eq(this.pageable))).thenReturn(
                new PageImpl<>(new ArrayList<>()));
        when(postConverter.toDto(ArgumentMatchers.<Page<PostEntity>>any())).thenReturn(
                PostPageDto.builder().build());

        PostPageDto result = postService.findPostByTitle("title", pageable);
        Assertions.assertThat(result).isInstanceOf(PostPageDto.class);
    }

    @Test
    void findPostByUsername() {
        when(postRepository.findByUsernameContaining(anyString(), eq(this.pageable))).thenReturn(
                new PageImpl<>(new ArrayList<>()));
        when(postConverter.toDto(ArgumentMatchers.<Page<PostEntity>>any())).thenReturn(postPageDto);

        PostPageDto result = postService.findPostByUsername("user", this.pageable);
        Assertions.assertThat(result).isEqualTo(postPageDto);
    }

    @Test
    void savePost() {
        setUserProv();
        when(postConverter.toEntity(any(PostDto.class), any(UserEntity.class))).thenReturn(
                postEntity);
        when(postRepository.save(any(PostEntity.class))).thenReturn(postEntity);
        when(postConverter.toDto(any(PostEntity.class))).thenReturn(postDto);

        PostDto result = postService.savePost(postDto);
        Assertions.assertThat(result).isEqualTo(postDto);
    }

    @Test
    void updatePost() {
        setUserProv();
        when(postRepository.findByPid(anyLong())).thenReturn(Optional.of(postEntity));
        when(postConverter.toDto(any(PostEntity.class))).thenReturn(postDto);

        PostDto result = postService.updatePost(postDto);
        Assertions.assertThat(result).isEqualTo(postDto);
    }

    @Test
    void likesPost() {
        setUserProv();
        when(postRepository.findByPid(any(Long.class))).thenReturn(Optional.of(postEntity));
        when(postLikeRepository.findByPidAndUid(any(PostEntity.class),
                any(UserEntity.class))).thenReturn(Optional.empty());
        when(postLikeRepository.save(any(PostLikeEntity.class))).thenReturn(null);

        Map<String,Object> result = postService.savelikeState(PostLikeDto.builder().pid(1L).likes(false).build());
        Assertions.assertThat(result.get("permit")).isEqualTo(true);
        verify(postLikeRepository,times(1)).save(any(PostLikeEntity.class));
    }

    @Test
    void deletePost() {
        setUserProv();
        when(postRepository.findByPid(anyLong())).thenReturn(Optional.of(postEntity));
        doNothing().when(postRepository).delete(any(PostEntity.class));

        Long result = postService.deletePost(1L);
        Assertions.assertThat(result).isEqualTo(1L);
        verify(postRepository, times(1)).delete(any(PostEntity.class));

    }

    @Test
    void deletePosts() {
        setUserProv();
        when(postRepository.findByPid(anyLong())).thenReturn(Optional.of(postEntity));

        int count = postService.deletePosts(Arrays.asList(1L, 2L));
        Assertions.assertThat(count).isEqualTo(2);
        verify(postRepository,times(1)).deleteAll(anyList());
    }

    private void setUserContextByUsername() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("user", null, null));
    }

    private void setUserProv() {
        setUserContextByUsername();
        when(userRepository.findByProvider(anyString())).thenReturn(Optional.of(userEntity));
    }

}