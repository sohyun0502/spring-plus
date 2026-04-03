package org.example.expert.domain.user.service;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.GetProfileImageUrlResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.dto.response.UserSearchResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCacheService userCacheService;
    private final S3Template s3Template;
    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofDays(7);

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    public UserSearchResponse searchUserByNickname(String nickname) {

        UserSearchResponse cached = userCacheService.getUserCache(nickname);

        if (cached != null) {
            log.info("Redis Data Cache Hit");
            return cached;
        }

        log.info("Redis Data Cache Miss {}", nickname);
        UserSearchResponse result  = userRepository.searchUserByNickname(nickname)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        userCacheService.saveUserCache(nickname, result);

        return result;
    }

    @Transactional
    public void uploadProfileImage(Long id, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        String key = "uploads/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            s3Template.upload(bucket, key, file.getInputStream());

        } catch (IOException e) {
            throw new InvalidRequestException("파일 업로드 실패");
        }

        user.updateProfileImageUrl(key);
        userRepository.save(user);
    }

    public GetProfileImageUrlResponse getProfileImagePresignedUrl(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("User not found"));


        String key = user.getProfileImageUrl();
        URL presignedUrl = s3Template.createSignedGetURL(bucket, key, PRESIGNED_URL_EXPIRATION);

        // 만료시간 계산
        Instant expirationTime = Instant.now().plus(PRESIGNED_URL_EXPIRATION);

        return new GetProfileImageUrlResponse(
                presignedUrl.toString(),
                expirationTime
        );
    }
}
