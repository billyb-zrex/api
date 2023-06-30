package com.sharefable.api.entity;

import com.sharefable.api.transport.resp.RespUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
    private String authId;

    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String email;

    private String avatar;

    private Boolean domainBlacklisted;

    private Long belongsToOrg;
}
