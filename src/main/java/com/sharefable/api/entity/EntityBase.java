package com.sharefable.api.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@MappedSuperclass
@Getter
@Setter
@ToString
public abstract class EntityBase {
    @CreationTimestamp
    protected Timestamp createdAt;

    @UpdateTimestamp
    protected Timestamp updatedAt;
}
