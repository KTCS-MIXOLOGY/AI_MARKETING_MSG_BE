package com.ai_marketing_msg_be.domain.message.service;

import com.ai_marketing_msg_be.domain.message.dto.GetToneMannerListResponse;
import com.ai_marketing_msg_be.domain.message.dto.ToneMannerResponse;
import com.ai_marketing_msg_be.domain.message.entity.ToneManner;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToneMannerService {

    public GetToneMannerListResponse getToneMannerList() {
        log.info("Getting tone manner list");

        List<ToneMannerResponse> toneManners = Arrays.stream(ToneManner.values())
                .map(ToneMannerResponse::from)
                .collect(Collectors.toList());

        log.info("Tone manner list retrieved: {} tones", toneManners.size());

        return GetToneMannerListResponse.of(toneManners);
    }
}