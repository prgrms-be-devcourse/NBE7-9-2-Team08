package com.backend.domain.evaluation.service;

import com.backend.domain.evaluation.dto.AiDto;
import com.backend.domain.evaluation.service.AiGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiGateway aiGateway;

    public AiDto.CompleteResponse complete(AiDto.CompleteRequest req) {
        String result = aiGateway.complete(req.content(), req.prompt());
        return new AiDto.CompleteResponse(result);
    }
}