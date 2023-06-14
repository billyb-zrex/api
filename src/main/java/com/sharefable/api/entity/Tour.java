package com.sharefable.api.entity;

import com.sharefable.api.transport.RespTourWithScreens;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "tour")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@TransportObjRef(cls = RespTourWithScreens.class)
public class Tour extends EntityBaseWithOwnership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String rid;

    @Column(nullable = false)
    private String assetPrefixHash;

    @Column(nullable = false)
    private String displayName;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(nullable = false, name = "created_by")
    private User createdBy;

    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "screens_tours_join",
        joinColumns = @JoinColumn(name = "tour_id"),
        inverseJoinColumns = @JoinColumn(name = "screen_id"))
    private Set<Screen> screens;
}
