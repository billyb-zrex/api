package com.sharefable.api.service;

import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient;
import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.Record;
import com.sharefable.api.config.FirehoseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.ByteBuffer;

@Service
@Slf4j
public class InsightService {
    private final AmazonKinesisFirehoseClient firehoseClient;

    private final FirehoseConfig firehoseConfig;

    @Autowired
    InsightService(AmazonKinesisFirehoseClient firehoseClient, FirehoseConfig firehoseConfig) {
        this.firehoseClient = firehoseClient;
        this.firehoseConfig = firehoseConfig;
    }

    public void sendEventsToFirehose(String sub, String userEventLogs) {
        try {
            PutRecordRequest putRecordRequest = new PutRecordRequest();
            String streamName = firehoseConfig.getStreamPrefix() + sub;
            putRecordRequest.setDeliveryStreamName(streamName);
            Record record = new Record().withData(ByteBuffer.wrap(userEventLogs.getBytes()));
            putRecordRequest.setRecord(record);
            firehoseClient.putRecord(putRecordRequest);
        } catch (Exception e) {
            log.warn("Something wrong while sending data to firehose");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while sending data to firehose");
        }
    }
}
