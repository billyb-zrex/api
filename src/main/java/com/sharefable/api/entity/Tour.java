package com.sharefable.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.Set;

@Entity
@Table(name = "tour")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Column(nullable = false)
    private String rId;

    @Column(nullable = false)
    private String assetPrefixHash;

    @Column(nullable = false)
    private String displayName;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(nullable = false, name = "created_by")
    private User createdBy;

    private String thumbnail;

    @Column(nullable = false)
    private Long belongToOrg;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "screens_tours_join",
        joinColumns = @JoinColumn(name = "tour_id"),
        inverseJoinColumns = @JoinColumn(name = "screen_id"))
    private Set<Screen> tours;
}
