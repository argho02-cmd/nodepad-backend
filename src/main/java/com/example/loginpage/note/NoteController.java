package com.example.loginpage.note;

import com.example.loginpage.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public List<NoteResponse> getNotes(@AuthenticationPrincipal User user) {
        return noteService.getNotes(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse createNote(@AuthenticationPrincipal User user, @RequestBody NoteRequest request) {
        return noteService.createNote(user, request);
    }

    @PutMapping("/{noteId}")
    public NoteResponse updateNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal User user,
            @RequestBody NoteRequest request
    ) {
        return noteService.updateNote(noteId, user, request);
    }

    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long noteId, @AuthenticationPrincipal User user) {
        noteService.deleteNote(noteId, user);
    }
}
