package com.sharefable.api.entity;

import com.sharefable.api.transport.resp.RespOrg;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "org")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@TransportObjRef(cls = RespOrg.class)
public class Org extends EntityBaseWithReadableId {
    @Column(nullable = false)
    private String displayName;

    private String thumbnail;

    @Column(nullable = false)
    private String domain;
}
