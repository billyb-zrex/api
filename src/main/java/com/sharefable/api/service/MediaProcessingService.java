package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Job;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.repo.JobRepo;
import com.sharefable.api.transport.*;
import com.sharefable.api.transport.req.ReqMediaProcessing;
import com.sharefable.api.transport.resp.MediaType;
import com.sharefable.api.transport.resp.RespMediaProcessingInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@Slf4j
public class MediaProcessingService {
  private final JobRepo repo;
  private final QMsgService qMsgService;
  private final EntityHoldingService entityHoldingService;

  @Autowired
  public MediaProcessingService(JobRepo repo, QMsgService qMsgService, EntityHoldingService entityHoldingService) {
    this.repo = repo;
    this.qMsgService = qMsgService;
    this.entityHoldingService = entityHoldingService;
  }

  private String getKeyPath(String path) {
    String keyPath = Utils.formKeyFromObjectUrl(path, 255);
    if (StringUtils.isBlank(keyPath)) {
      log.error("Can't send process request as keyPath can't be formed");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path not legal");
    }
    return keyPath;
  }

  private Job submitJob(String keyPath, JobType jobType, JobProcessingInfo jobInfo) {
    Job job = Job.builder()
      .jobType(jobType)
      .jobKey(keyPath)
      .processingStatus(JobProcessingStatus.Touched)
      .info(jobInfo)
      .build();
    Job savedJob = repo.save(job);

    qMsgService.sendSqsMessage(jobInfo.getType(), jobInfo);
    return savedJob;
  }

  private RespMediaProcessingInfo transcodeVideoWithFormat(VideoProcessingSub sub, String basePath) {
    String processedPath = switch (sub) {
      case CONVERT_TO_MP4 -> String.format("%s.mp4", basePath);
      case CONVERT_TO_HLS -> String.format("%s_hls/master", basePath);
    };

    MediaType mediaType = switch (sub) {
      case CONVERT_TO_MP4 -> MediaType.VIDEO_MP4;
      case CONVERT_TO_HLS -> MediaType.VIDEO_HLS;
    };


    String keyPath = getKeyPath(processedPath);
    VideoTranscodingJobInfo jobInfo = VideoTranscodingJobInfo.builder()
      .sourceFilePath(basePath)
      .sub(sub)
      .key(keyPath)
      .processedFilePath(processedPath)
      .build();

    Job submittedJob = submitJob(keyPath, JobType.TRANSCODE_VIDEO, jobInfo);

    String processedPathForClient = processedPath;
    if (sub == VideoProcessingSub.CONVERT_TO_HLS) {
      processedPathForClient += ".m3u8"; // send the file extension to client
    }
    return RespMediaProcessingInfo.builder()
      .processingState(JobProcessingStatus.Touched)
      .originalFilePath(basePath)
      .jobId(submittedJob.getId())
      .mediaType(mediaType)
      .processedFilePath(processedPathForClient)
      .createdAt(submittedJob.getCreatedAt())
      .updatedAt(submittedJob.getUpdatedAt())
      .build();
  }

  @Transactional
  public RespMediaProcessingInfo[] transcodeVideoForStreaming(ReqMediaProcessing body) {
    RespMediaProcessingInfo mp4JobInfo = transcodeVideoWithFormat(VideoProcessingSub.CONVERT_TO_MP4, body.getPath());
    RespMediaProcessingInfo hlsJobInfo = transcodeVideoWithFormat(VideoProcessingSub.CONVERT_TO_HLS, body.getPath());

    String keyPath = getKeyPath(body.getPath());
    entityHoldingService.addAssociation(
      body.getAssn(),
      keyPath,
      MediaTypeEntityHolding.builder()
        .fullFilePaths(new String[]{
          body.getPath(),
          mp4JobInfo.getProcessedFilePath(),
          hlsJobInfo.getProcessedFilePath()
        })
        .build()
    );

    return new RespMediaProcessingInfo[]{hlsJobInfo, mp4JobInfo};
  }

  @Transactional
  public RespMediaProcessingInfo resizeImage(ReqMediaProcessing body, String resolution) {
    String originalFilePath = body.getPath();
    String processedFilePath = Utils.appendSuffixAfterFilename(originalFilePath, resolution);
    String keyPath = getKeyPath(processedFilePath);

    ImgResizingJobInfo jobInfo = ImgResizingJobInfo.builder()
      .sourceFilePath(originalFilePath)
      .processedFilePath(processedFilePath)
      .key(keyPath)
      .resolution(resolution)
      .build();

    entityHoldingService.addAssociation(
      body.getAssn(),
      keyPath,
      MediaTypeEntityHolding.builder()
        .fullFilePaths(new String[]{originalFilePath, processedFilePath})
        .build()
    );

    Job submittedJob = submitJob(keyPath, JobType.RESIZE_IMG, jobInfo);

    return RespMediaProcessingInfo.builder()
      .processingState(JobProcessingStatus.Touched)
      .originalFilePath(body.getPath())
      .jobId(submittedJob.getId())
      .mediaType(MediaType.IMG_MULTI)
      .processedFilePath(processedFilePath)
      .createdAt(submittedJob.getCreatedAt())
      .updatedAt(submittedJob.getUpdatedAt())
      .build();
  }

  @Transactional
  public void generateDemoGif(Tour tour, AssetFilePath manifestPath, AssetFilePath gifPath) {
    String key = String.format("%d:%s:%s:%d", tour.getPublishedVersion() + 1, tour.getRid(), JobType.CREATE_DEMO_GIF, Instant.now().getEpochSecond());
    CreateGifJobInfo info = CreateGifJobInfo.builder()
      .manifestFilePath(manifestPath.getBucketUriToFile())
      .gifFilePath(gifPath.getBucketUriToFile())
      .key(key)
      .build();

    submitJob(key, JobType.CREATE_DEMO_GIF, info);
  }
}
