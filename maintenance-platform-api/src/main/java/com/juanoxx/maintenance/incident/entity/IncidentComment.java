package com.juanoxx.maintenance.incident.entity;

import com.juanoxx.maintenance.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "incident_comments")
public class IncidentComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false, length = 40)
    private CommentType commentType;

    @Column(nullable = false, length = 2500)
    private String message;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
