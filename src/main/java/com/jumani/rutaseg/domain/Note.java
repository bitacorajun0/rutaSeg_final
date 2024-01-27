package com.jumani.rutaseg.domain;

import com.jumani.rutaseg.util.DateGen;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "notes")
@EqualsAndHashCode(exclude = "id")
public class Note implements DateGen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author")
    @Enumerated(EnumType.STRING)
    private Author author;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    private Note() {
    }


    public Note(Author author, String content, Long createByUserId) {
        this.author = author;
        this.content = content;
        this.createdByUserId = createByUserId;
        this.createdAt = this.currentDateUTC();
    }

    public void update(@NonNull String content) {
        this.content = content;
        this.updatedAt = this.currentDateUTC();
    }

    public boolean isClient() {
        return Author.CLIENT.equals(this.author);
    }

    public boolean isSystem() {
        return Author.SYSTEM.equals(this.author);
    }
}
