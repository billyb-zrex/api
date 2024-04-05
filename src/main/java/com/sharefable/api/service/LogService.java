package com.sharefable.api.service;

import com.sharefable.api.entity.Log;
import com.sharefable.api.repo.LogRepo;
import com.sharefable.api.transport.ReqNewLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogService {
  private final LogRepo logRepo;

  @Transactional
  public void appendNewLogLine(ReqNewLog req) {
    Log line = Log.builder()
      .orgId(req.orgId())
      .logType(req.logType())
      .forObjectType(req.forObjectType())
      .forObjectId(req.forObjectId())
      .logLine(req.logLine())
      .build();

    logRepo.save(line);
  }
}
