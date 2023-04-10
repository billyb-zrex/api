package com.sharefable.api.entity;

import com.sharefable.api.transport.RespOrg;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "org")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@TransportObjRef(cls = RespOrg.class)
public class Org extends EntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String rid;

    @Column(nullable = false)
    private String displayName;

    private String thumbnail;

    @Column(nullable = false)
    private String domain;
}
