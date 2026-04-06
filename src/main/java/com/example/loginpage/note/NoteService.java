package com.example.loginpage.note;

import com.example.loginpage.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    public List<NoteResponse> getNotes(User user) {
        return noteRepository.findAllByOwnerIdOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NoteResponse createNote(User user, NoteRequest request) {
        var content = normalizeContent(request.getContent());
        var note = Note.builder()
                .title(buildTitle(content))
                .content(content)
                .owner(user)
                .build();
        return toResponse(noteRepository.save(note));
    }

    public NoteResponse updateNote(Long noteId, User user, NoteRequest request) {
        var note = noteRepository.findByIdAndOwnerId(noteId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
        var content = normalizeContent(request.getContent());
        note.setContent(content);
        note.setTitle(buildTitle(content));
        return toResponse(noteRepository.save(note));
    }

    public void deleteNote(Long noteId, User user) {
        var note = noteRepository.findByIdAndOwnerId(noteId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
        noteRepository.delete(note);
    }

    private String normalizeContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note content cannot be empty");
        }
        return content.trim();
    }

    private String buildTitle(String content) {
        return content.length() <= 25 ? content : content.substring(0, 25);
    }

    private NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
