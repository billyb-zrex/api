package com.sharefable.api.common;

import com.sharefable.api.transport.GenerateTSDef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@GenerateTSDef
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dataset {
  private String name;
  private Integer lastPublishedVersion;
  private Timestamp lastPublishedDate;
}
