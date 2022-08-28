package com.sharefable.appserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "project")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Project {
    public enum FieldRef {DisplayName, Thumbnail}

    @JsonIgnore
    public static final List<FieldRef> UPDATABLE_FIELDS = Arrays.stream(new FieldRef[]{
        FieldRef.DisplayName,
        FieldRef.Thumbnail
    }).collect(Collectors.toList());

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
