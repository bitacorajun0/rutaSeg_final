package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.Author;
import com.jumani.rutaseg.domain.Note;
import com.jumani.rutaseg.domain.Order;
import com.jumani.rutaseg.dto.request.NoteRequest;
import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.dto.result.PaginatedResult;
import com.jumani.rutaseg.exception.ForbiddenException;
import com.jumani.rutaseg.exception.NotFoundException;
import com.jumani.rutaseg.exception.ValidationException;
import com.jumani.rutaseg.handler.Session;
import com.jumani.rutaseg.repository.OrderRepository;
import com.jumani.rutaseg.util.PaginationUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders/{orderId}/notes")
public class NoteController {
    private final OrderRepository orderRepo;

    @GetMapping
    public ResponseEntity<PaginatedResult<Note>> search(@PathVariable("orderId") long orderId,
                                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                                        @RequestParam(value = "page_size", defaultValue = "100") int pageSize,
                                                        @Session SessionInfo session) {

        final Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        if (!session.admin() && !Objects.equals(order.getClient().getUserId(), session.userId())) {
            throw new ForbiddenException();
        }

        final List<Note> notes = order.getNotes();
        final PaginatedResult<Note> result = PaginationUtil.get(notes.size(), pageSize, page,
                (offset, limit) -> notes.stream()
                        .sorted(Comparator.comparing(Note::getCreatedAt))
                        .skip(offset)
                        .limit(limit)
                        .toList()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Note> addNoteToOrder(@PathVariable("orderId") long orderId,
                                               @RequestBody @Valid NoteRequest noteRequest,
                                               @Session SessionInfo session) {

        final Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        if (!session.admin() && !Objects.equals(order.getClient().getUserId(), session.userId())) {
            throw new ForbiddenException();
        }

        final Author author = session.admin() ? Author.ADMIN : Author.CLIENT;

        final Note note = new Note(author, noteRequest.getContent(), session.userId());
        order.addNote(note);

        final Order updatedOrder = orderRepo.save(order);
        final Note createdNote = updatedOrder.getNotes().stream().filter(note::equals).findFirst().orElseThrow();

        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<Note> updateNote(@PathVariable("orderId") long orderId,
                                           @PathVariable("noteId") long noteId,
                                           @RequestBody @Valid NoteRequest noteRequest,
                                           @Session SessionInfo session) {

        final Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        if (!session.admin() && !Objects.equals(order.getClient().getUserId(), session.userId())) {
            throw new ForbiddenException();
        }

        final Note note = order.findNote(noteId)
                .orElseThrow(() -> new NotFoundException(String.format("note with id [%s] not found in order [%s]", noteId, orderId)));

        if (!session.admin() && !note.isClient()) {
            throw new ForbiddenException();
        }

        if (note.isSystem()) {
            throw new ValidationException("note_not_updatable", "system notes cannot be updated");
        }

        note.update(noteRequest.getContent());

        orderRepo.save(order);

        return ResponseEntity.ok(note);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable("orderId") long orderId,
                                        @PathVariable("noteId") long noteId,
                                        @Session SessionInfo session) {

        final Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        if (!session.admin() && !Objects.equals(order.getClient().getUserId(), session.userId())) {
            throw new ForbiddenException();
        }

        order.removeNote(noteId)
                .stream()
                .peek(note -> {
                    if (!session.admin() && !note.isClient()) {
                        throw new ForbiddenException();
                    }

                    if (note.isSystem()) {
                        throw new ValidationException("note_not_deletable", "system notes cannot be deleted");
                    }

                }).findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("note with id [%s] not found in order [%s]", noteId, orderId)));

        orderRepo.save(order);

        return ResponseEntity.noContent().build();
    }
}
