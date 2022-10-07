package com.sharefable.api.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Map;

@Entity
@Table(name = "asset_mapping")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@TypeDef(name = "json", typeClass = JsonType.class)
public class AssetMapping {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String assetName;

    @Column(nullable = false)
    private String assetPath;

    @Column(nullable = false)
    private String origin;

    @Column(name = "http_status", nullable = false)
    private HttpStatus status;

    @Column(nullable = false)
    private HttpMethod method;

    private String contentType;

    @Column(columnDefinition = "json")
    @Type(type = "json")
    private Map<String, Object> meta;
}
