package com.backend.domain.repository.dto.response.github;

// issue 응답용 DTO
public record IssueResponse(
        Long number,
        String title,
        String state,
        String created_at,
        String closed_at,
        PullRequest pull_request
) {
    public record PullRequest(String url) {}

    public boolean isPureIssue() {
        return pull_request == null;
    }

    public boolean isClosed() {
        return "closed".equals(state);
    }
}
