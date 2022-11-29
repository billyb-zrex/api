package com.sharefable.api.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Map;

@Entity
@Table(name = "asset_proxy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProxyAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Column(nullable = false)
    private String rid;

    @Column(nullable = false)
    private String fullOriginUrl;

    @Column(nullable = false)
    private String proxyUri;

    @Column(nullable = false, name = "belongs_to_proj")
    private Long belongsToProj;

    @Column(columnDefinition = "json")
    @Type(type = "json")
    private Map<String, String> meta;
}
