package com.sharefable.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "tour")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Tour extends EntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

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
