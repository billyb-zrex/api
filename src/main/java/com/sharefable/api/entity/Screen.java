package com.sharefable.api.entity;

import com.sharefable.api.transport.ScreenType;
import com.sharefable.api.transport.resp.RespScreen;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "screen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@TransportObjRef(cls = RespScreen.class)
public class Screen extends EntityBaseWithOwnership {
    @Column(nullable = false)
    private String assetPrefixHash;

    @Column(nullable = false)
    private String displayName;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(nullable = false, name = "created_by")
    private User createdBy;

    private String thumbnail;

    @Column(nullable = false)
    private Long parentScreenId;

    @Column(nullable = false)
    private String url;

    private String icon;

    @Column(nullable = false)
    private Boolean responsive;

    // Read: com.sharefable.api.transport.ScreenType doc
    @Enumerated(value = EnumType.ORDINAL)
    @Column(nullable = false)
    private ScreenType type;

    // NOTE Although screen <-> tour is defined as many-to-many relationship in database, from logical standpoint
    //      this is a many-one relationship (for the time being). The reason we kept it as many-to-many relationship
    //      in persistent layer is because we initially thought that one screen can be part of many tours, while that
    //      is logically sound, it has its own implication in UX.
    //      We might need many-to-many relationship down the line once we introduce the concept of a single screen
    //      sharing multiple tour with better UX.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "screens_tours_join",
        joinColumns = @JoinColumn(name = "screen_id"),
        inverseJoinColumns = @JoinColumn(name = "tour_id"))
    private Set<Tour> tours;
}
