package com.example.loginpage.note;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByOwnerIdOrderByUpdatedAtDesc(int ownerId);

    Optional<Note> findByIdAndOwnerId(Long id, int ownerId);
}
