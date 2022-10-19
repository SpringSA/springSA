package com.example.sa_advanced.service;

import com.example.sa_advanced.controller.response.PostResponseDto;
import com.example.sa_advanced.controller.response.ResponseDto;
import com.example.sa_advanced.domain.LikePost;
import com.example.sa_advanced.domain.Member;
import com.example.sa_advanced.domain.Post;
import com.example.sa_advanced.jwt.TokenProvider;
import com.example.sa_advanced.repository.LikePostRepository;
import com.example.sa_advanced.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikePostService {
    private final PostRepository postRepository;
    private final LikePostRepository likePostRepository;

    private final TokenProvider tokenProvider;
    @Transactional
    public ResponseDto<?> likePost(Long post_id, HttpServletRequest request){
        if (null == request.getHeader("Refresh-Token")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        if (null == request.getHeader("Authorization")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        Member member = validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        //
        Post post = postRepository.findById(post_id).orElse(null);
        if(post == null) return ResponseDto.fail("NOT_FOUND","게시글을 찾을 수 없습니다.");

        Optional<LikePost> like = likePostRepository.findByMemberAndPost(member,post);
//        System.out.println(like.getId());

        System.out.println(like);
        if(!like.isPresent()){
            // 좋아요
            LikePost newlike = new LikePost();
            newlike.setMember(member);
            newlike.setPost(post);
            post.getLikePosts().add(newlike);
            likePostRepository.save(newlike);
        }
        else {
            // 좋아요 취소
            System.out.println("here");
//            post.getLikePosts().remove(like);
//            member.getLikePosts().remove(like);
            likePostRepository.delete(like.get());
//            likePostRepository.deleteById(5L);
        }


        return ResponseDto.success(
                PostResponseDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .member(post.getMember().getNickname())
                        .likeCount(post.countLike())
                        .createdAt(post.getCreatedAt())
                        .modifiedAt(post.getModifiedAt())
                        .build()
        );
    }

    @Transactional
    public Member validateMember(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("Refresh-Token"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }
}

