package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.CommitResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CommitInfoMapper {
    // ResponseData 유지보수성 [커밋 관련]
    public void mapCommitInfo(RepositoryData data, List<CommitResponse> response) {
        if (response == null || response.isEmpty()) {
            setDefaultValues(data);
            return;
        }

        // 마지막 커밋 시점
        LocalDateTime lastCommitDate = parseCommitDate(response.get(0).commit().author().date());
        data.setLastCommitDate(lastCommitDate);

        // 마지막 커밋 이후 경과일
        int daysSince = calculateDaysSinceLastCommit(lastCommitDate);
        data.setDaysSinceLastCommit(daysSince);

        // 최근 90일 커밋 수
        data.setCommitCountLast90Days(response.size());

        // 최근 10개 커밋 메시지 - 일관성 판단
        List<RepositoryData.CommitInfo> recentCommits = extractRecentCommitMessages(response);
        data.setRecentCommits(recentCommits);
    }

    private void setDefaultValues(RepositoryData data) {
        data.setLastCommitDate(null);
        data.setDaysSinceLastCommit(0);
        data.setCommitCountLast90Days(0);
        data.setRecentCommits(Collections.emptyList());
    }

    private LocalDateTime parseCommitDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return LocalDateTime.now();
        }

        try {
            ZonedDateTime utcTime = ZonedDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
            ZonedDateTime koreaTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
            return koreaTime.toLocalDateTime();
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }

    private int calculateDaysSinceLastCommit(LocalDateTime date) {
        if(date == null) {
            return 0;
        }

        long daysBetween = ChronoUnit.DAYS.between(date, LocalDateTime.now());
        return Math.max(0, (int) daysBetween);
    }

    private List<RepositoryData.CommitInfo> extractRecentCommitMessages(List<CommitResponse> commitResponses) {
        return commitResponses.stream()
                .limit(10)
                .map(this::createCommitInfoFromMessage)
                .collect(Collectors.toList());
    }

    private RepositoryData.CommitInfo createCommitInfoFromMessage(CommitResponse commitResponse) {
        RepositoryData.CommitInfo commitInfo = new RepositoryData.CommitInfo();

        String message = Optional.ofNullable(commitResponse.commit().message())
                .map(String::trim)
                .map(this::cleanCommitMessage)
                .filter(msg -> !msg.isEmpty())
                .orElse("No commit message");

        commitInfo.setMessage(message);

        if (commitResponse.commit().author() != null) {
            LocalDateTime commitDate = parseCommitDate(commitResponse.commit().author().date());
            commitInfo.setCommittedDate(commitDate);
        }

        return commitInfo;
    }

    private String cleanCommitMessage(String message) {
        String[] lines = message.split("\n");
        String firstLine = lines[0].trim();

        if (firstLine.length() > 100) {
            return firstLine.substring(0, 97) + "...";
        }

        return firstLine;
    }
}
