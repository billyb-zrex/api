package com.sharefable.api.entity;

import com.sharefable.api.transport.RespUser;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@TransportObjRef(cls = RespUser.class)
public class User extends EntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String email;

    private String avatar;

    // User <-> Org would almost always be loaded together as both information are needed almost instant basis.
    // More over when user gets serialized org would also be needed, and if that serialization happens outside jpa
    // session it would throw an error.
    // This is not a costly operation as it performs a one <-> one join (per user entity) on index.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(nullable = false, name = "belongs_to_org")
    private Org belongsToOrg;
}
