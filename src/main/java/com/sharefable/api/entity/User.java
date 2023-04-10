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

    private String authId;

    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String email;

    private String avatar;

    private Boolean domainBlacklisted;

    private Long belongsToOrg;
}
