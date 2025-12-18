package com.ai_marketing_msg_be.domain.message.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ToneManner {
    FRIENDLY("TONE001", "친근한", "친구처럼 편안하고 가까운 느낌",
            "안녕하세요! 요즘 날씨가 추운데 건강 잘 챙기고 계시죠?"),

    POLITE("TONE002", "공손한", "격식 있고 정중한 표현",
            "안녕하십니까, 고객님. 항상 저희 서비스를 이용해주셔서 감사드립니다."),

    HUMOROUS("TONE003", "유머러스한", "재치있고 유쾌한 분위기",
            "여기요! 주머니가 가벼워지는 마법 같은 혜택을 들고 왔어요 🎉"),

    PROFESSIONAL("TONE004", "전문적인", "신뢰감 있고 전문가다운 표현",
            "고객님의 데이터 분석 결과, 최적화된 상품을 제안드립니다."),

    URGENT("TONE005", "긴급한", "시급성을 강조하는 표현",
            "⚠️ 마감 임박! 오늘 자정까지만 특별 혜택을 받으실 수 있습니다.");

    private final String toneId;
    private final String toneName;
    private final String description;
    private final String example;

    public static ToneManner fromToneId(String toneId) {
        for (ToneManner tone : values()) {
            if (tone.getToneId().equals(toneId)) {
                return tone;
            }
        }
        return null;
    }


    public static ToneManner fromToneName(String toneName) {
        for (ToneManner tone : values()) {
            if (tone.getToneName().equals(toneName)) {
                return tone;
            }
        }
        return null;
    }
}