package com.sharefable.analytics.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "activity", schema = "al")
@Getter
@Setter
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
public class Activity extends ActivityBase {
}
