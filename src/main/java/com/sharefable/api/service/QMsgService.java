package com.sharefable.api.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.sharefable.api.common.MapSerializable;
import com.sharefable.api.config.SQSConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class QMsgService {
    private final SQSConfig config;
    private final AmazonSQS sqsClient;

    @Autowired
    public QMsgService(SQSConfig config, ObjectProvider<AmazonSQS> sqsClientProvider) {
        this.config = config;
        this.sqsClient = sqsClientProvider.getIfAvailable();
    }

    private SendMessageRequest getProducibleMsg(String key, Map<String, String> payload) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
            .withQueueUrl(config.getQUrl())
            .withMessageBody(key);

        Map<String, MessageAttributeValue> msgAttrs = new HashMap<>();
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            msgAttrs.put(entry.getKey(),
                new MessageAttributeValue().withStringValue(entry.getValue()).withDataType("String"));
        }
        return sendMessageRequest.withMessageAttributes(msgAttrs);
    }

    public void sendSqsMessage(String key, Map<String, String> payload) {
        if (!config.isEnabled() || sqsClient == null) {
            log.info("Skipping background job {} because SQS is disabled", key);
            return;
        }
        SendMessageRequest msgReq = getProducibleMsg(key, payload);
        SendMessageResult sendMessageResult = sqsClient.sendMessage(msgReq);
        log.debug("Message {} posted in sqs", sendMessageResult.getMessageId());
    }

    public void sendSqsMessage(String key, MapSerializable payload) {
        sendSqsMessage(key, payload.toMap());
    }
}
