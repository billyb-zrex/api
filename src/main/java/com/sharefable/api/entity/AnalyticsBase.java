package com.sharefable.api.entity;

import com.sharefable.api.transport.EntryDuratinType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AnalyticsBase extends EntityBase {

    @Column(nullable = false)
    private Long tourId;

    @Column(nullable = false)
    private String dateYmd;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private EntryDuratinType entryDurationType;
}
