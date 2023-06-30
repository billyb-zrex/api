package com.sharefable.api.service;

import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.sharefable.api.common.ExcludeEmailDomain;
import com.sharefable.api.config.SQSConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class QMsgService {
    private final SQSConfig config;

    @Autowired
    public QMsgService(SQSConfig config) {
        this.config = config;
    }

    public SendMessageRequest getProducibleMsg(String key, ExcludeEmailDomain.MapSerializable payload) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
            .withQueueUrl(config.getQUrl())
            .withMessageBody(key);

        Map<String, String> props = payload.toMap();
        Map<String, MessageAttributeValue> msgAttrs = new HashMap<>();
        for (Map.Entry<String, String> entry : props.entrySet()) {
            msgAttrs.put(entry.getKey(),
                new MessageAttributeValue().withStringValue(entry.getValue()).withDataType("String"));
        }
        return sendMessageRequest.withMessageAttributes(msgAttrs);
    }
}
