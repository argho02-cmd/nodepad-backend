package com.example.loginpage.note;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NoteResponse(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
