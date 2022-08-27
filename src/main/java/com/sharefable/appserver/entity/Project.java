package com.sharefable.appserver.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "project")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Project {
    private static final String SEQUENCE_NAME = "project_sequence";

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;


    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String displayName;

    private String thumbnail;
}
