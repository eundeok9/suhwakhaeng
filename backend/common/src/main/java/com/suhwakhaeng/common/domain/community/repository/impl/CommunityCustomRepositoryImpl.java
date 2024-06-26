package com.suhwakhaeng.common.domain.community.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.suhwakhaeng.common.domain.community.dto.CommunityDetailResponse;
import com.suhwakhaeng.common.domain.community.dto.CommunitySearchRequest;
import com.suhwakhaeng.common.domain.community.dto.CommunityListResponse;
import com.suhwakhaeng.common.domain.community.dto.WriterInfo;
import com.suhwakhaeng.common.domain.community.enums.Category;
import com.suhwakhaeng.common.domain.community.repository.CommunityCustomRepository;
import com.suhwakhaeng.common.global.util.NullSafeBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.suhwakhaeng.common.domain.community.entity.QCommunity.*;
import static com.suhwakhaeng.common.domain.community.entity.QCommunityComment.*;
import static com.suhwakhaeng.common.domain.community.entity.QCommunityLike.*;
import static com.suhwakhaeng.common.domain.user.entity.QUser.*;

@Repository
@RequiredArgsConstructor
public class CommunityCustomRepositoryImpl implements CommunityCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public CommunityDetailResponse selectCommunityDetail(Long userId, Long communityId) {
        return queryFactory
                .select(Projections.fields(CommunityDetailResponse.class,
                        Projections.fields(WriterInfo.class,
                                user.id.as("userId"),
                                user.nickname.as("nickname"),
                                user.profileImage.as("profileImage")
                        ).as("user"),
                        community.id.as("communityId"),
                        community.cate.as("cate"),
                        community.content.as("communityContent"),
                        community.image1.as("image1"),
                        community.image2.as("image2"),
                        community.image3.as("image3"),
                        community.image4.as("image4"),
                        ExpressionUtils.as(
                                JPAExpressions.selectOne()
                                        .from(communityLike)
                                        .where(communityLike.communityLikePK.community.id.eq(community.id)
                                                .and(communityLike.communityLikePK.user.id.eq(userId)))
                                        .exists(),
                                "isLiked"
                        ),
                        ExpressionUtils.as(
                                JPAExpressions.select(communityLike.count())
                                        .from(communityLike)
                                        .where(communityLike.communityLikePK.community.eq(community)),
                                "likeCount"
                        ),
                        ExpressionUtils.as(
                                JPAExpressions.select(communityComment.count())
                                        .from(communityComment)
                                        .where(communityComment.community.eq(community)),
                                "commentCount"
                        ),
                        community.createdAt.as("createdAt")
                ))
                .from(community)
                .join(community.writer, user)
                .where(community.id.eq(communityId))
                .fetchOne();
    }

    @Override
    public List<CommunityListResponse> selectMyCommunity(Long userId, Long lastId) {
        return queryFactory
                .select(Projections.fields(CommunityListResponse.class,
                        Projections.fields(WriterInfo.class,
                                user.id.as("userId"),
                                user.nickname.as("nickname"),
                                user.profileImage.as("profileImage")
                        ).as("user"),
                        community.id.as("communityId"),
                        community.content.as("communityContent"),
                        community.image1.as("thumbnail"),
                        community.cate.as("cate"),
                        ExpressionUtils.as(
                                JPAExpressions.selectOne()
                                        .from(communityLike)
                                        .where(communityLike.communityLikePK.community.id.eq(community.id)
                                                .and(communityLike.communityLikePK.user.id.eq(userId)))
                                        .exists(),
                                "isLiked"
                        ),
                        ExpressionUtils.as(
                                JPAExpressions.select(communityLike.count())
                                        .from(communityLike)
                                        .where(communityLike.communityLikePK.community.eq(community)),
                                "likeCount"
                        ),
                        ExpressionUtils.as(
                                JPAExpressions.select(communityComment.count())
                                        .from(communityComment)
                                        .where(communityComment.community.eq(community)),
                                "commentCount"
                        ),
                        community.createdAt.as("createdAt")
                ))
                .from(community)
                .join(community.writer, user)
                .where(isLowerThan(lastId), isEqualsUserId(userId))
                .orderBy(community.createdAt.desc())
                .limit(10)
                .fetch();
    }

    private BooleanBuilder isEqualsUserId(final Long userId) {
        return NullSafeBuilder.build(() -> user.id.eq(userId));
    }

    @Override
    public List<CommunityListResponse> searchCommunity(Long userId, CommunitySearchRequest request) {
        return queryFactory
                .select(Projections.fields(CommunityListResponse.class,
                        Projections.fields(WriterInfo.class,
                                user.id.as("userId"),
                                user.nickname.as("nickname"),
                                user.profileImage.as("profileImage")
                        ).as("user"),
                        community.id.as("communityId"),
                        community.content.as("communityContent"),
                        community.image1.as("thumbnail"),
                        community.cate.as("cate"),
                        ExpressionUtils.as(
                                JPAExpressions.selectOne()
                                        .from(communityLike)
                                        .where(communityLike.communityLikePK.community.id.eq(community.id)
                                                .and(communityLike.communityLikePK.user.id.eq(userId)))
                                        .exists(),
                                "isLiked"
                        ),
                        ExpressionUtils.as(
                                JPAExpressions.select(communityLike.count())
                                        .from(communityLike)
                                        .where(communityLike.communityLikePK.community.eq(community)),
                                "likeCount"
                        ),
                        ExpressionUtils.as(
                                JPAExpressions.select(communityComment.count())
                                        .from(communityComment)
                                        .where(communityComment.community.eq(community)),
                                "commentCount"
                        ),
                        community.createdAt.as("createdAt")
                ))
                .from(community)
                .join(community.writer, user)
                .where(isLowerThan(request.id()), contentLikeKeyword(request.keyword()), equalsCate(request.cate()))
                .orderBy(community.createdAt.desc())
                .limit(10)
                .fetch();
    }

    private BooleanBuilder isLowerThan(final Long communityId) {
        BooleanBuilder builder = new BooleanBuilder();
        if (communityId != null && communityId > 0) {
            builder.and(community.id.lt(communityId));
        }
        return builder;
    }

    private BooleanBuilder contentLikeKeyword(final String keyword) {
        return NullSafeBuilder.build(() -> community.content.like("%" + keyword + "%"));
    }

    private BooleanBuilder equalsCate(final Category cate) {
        return NullSafeBuilder.build(() -> community.cate.eq(cate));
    }
}
