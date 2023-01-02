package com.sharefable.api.entity;

import com.sharefable.api.transport.RespProxyAsset;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asset_proxy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@TransportObjRef(cls = RespProxyAsset.class)
public class ProxyAsset extends EntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String rid;

    @Column(nullable = false)
    private String fullOriginUrl;

    private String proxyUri;

    @Column(nullable = false)
    private Integer httpStatus;
}
