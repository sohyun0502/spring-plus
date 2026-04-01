package org.example.expert.domain.user.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.dto.response.UserSearchResponse;

import java.util.Optional;

import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<UserSearchResponse> searchUserByNickname(String nickname) {
        UserSearchResponse result = queryFactory
                .select(Projections.constructor(UserSearchResponse.class,
                        user.id,
                        user.nickname
                ))
                .from(user)
                .where(user.nickname.eq(nickname))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
