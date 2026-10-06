package com.example.board.global.redis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class RedisKeys {

    private static final String PREFIX = "board:";

    private RedisKeys() {

    }

    // 좋아요: 게시글별 좋아요 누른 회원 Set (DB 원본, TTL 1시간 캐시)
    public static String postLikes(Long postId) {
        return PREFIX + "post:" + postId + ":likes";
    }

    // 랭킹: 일별 좋아요 증가량 Sorted Set (TTL 8일)
    public static String dailyLikeRanking(LocalDate data) {
        return PREFIX + "ranking:likes:" + data.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    // 랭킹: 최근 7일 합산 결과 Sorted Set (1분마다 ZUNIONSTORE로 재생성)
    public static String weeklyLikeRanking() {
        return PREFIX + "ranking:likes:weekly";
    }

    // 조회수: DB 반영 대기 중인 증가분 Sorted Set (TTL 없음, 원본 성격)
    public static String pendingViews() {
        return PREFIX + "views:pending";
    }

    // 조회수 중복 방지: 회원 (SET NX EX 600)
    public static String viewDedupForMember(Long postId, Long memberId) {
        return PREFIX + "view:post:" + postId + ":member:" + memberId;
    }

    // 조회수 중복 방지: 비회원 visitor 쿠키 (SET NX EX 600)
    public static String viewDedupForVisitor(Long postId, String visitorId) {
        return PREFIX + "view:post:" + postId + ":visitor:" + visitorId;
    }

}
