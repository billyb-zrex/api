package com.sharefable.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.transport.NfEvents;
import com.sharefable.api.transport.req.ReqNfHook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Service
@Slf4j
public class NfHookService {
    private static final HashSet<NfEvents> ALLOWED_EVENTS = new HashSet<>();
    private final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        ALLOWED_EVENTS.add(NfEvents.EBOOK_DOWNLOAD);
        ALLOWED_EVENTS.add(NfEvents.NEW_USER_SIGNUP);
    }

    private final QMsgService qMsgService;

    @Autowired
    public NfHookService(QMsgService qMsgService) {
        this.qMsgService = qMsgService;
    }

    public boolean sendNotification(ReqNfHook body) {
        return sendNotification(body.eventName(), body.payload());
    }

    public boolean sendNotification(NfEvents eventName, Map<String, String> payload) {
        if (!ALLOWED_EVENTS.contains(eventName)) {
            log.warn("{} is sent for notification, but is discarded as ALLOWED_EVENTS does not contain the event", payload);
            return false;
        }

        Map<String, String> msg = new HashMap<>();
        msg.put("eventName", eventName.toString());

        Map<String, String> msgPayload = objectMapper.convertValue(payload, Map.class);
        for (Map.Entry<String, String> entry : msgPayload.entrySet()) {
            msg.put("payload_" + entry.getKey(), entry.getValue());
        }

        qMsgService.sendSqsMessage("NF", msg);
        return true;
    }
}
