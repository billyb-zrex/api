package com.sharefable.appserver.common.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.net.MalformedURLException;
import java.net.URL;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class NewProxyAssetReqBodyParsed extends NewProxyAssetReqBody {
    private MediaType contentType;
    private HttpStatus status;
    private URL origin;
    private URL url;

    public static final String FALLBACK_MEDIA_TYPE = "fable/fallback-mime";

    public static NewProxyAssetReqBodyParsed from(NewProxyAssetReqBody rawBody) throws MalformedURLException {
        NewProxyAssetReqBodyParsed parsedBody = new NewProxyAssetReqBodyParsed();

        parsedBody.setMethod(rawBody.getMethod());
        parsedBody.setRespBody(rawBody.getRespBody());
        parsedBody.setOrigin(new URL(rawBody.getOriginNotParsed()));
        parsedBody.setUrl(new URL(rawBody.getUrlNotParsed()));
        parsedBody.setReqHeaders(rawBody.getReqHeaders());
        parsedBody.setRespHeaders(rawBody.getRespHeaders());

        HttpStatus status = HttpStatus.resolve(rawBody.getStatusNotParsed());
        parsedBody.setStatus(status);

        MediaType parsedMediaType;
        try {
           parsedMediaType = MediaType.parseMediaType(rawBody.getContentTypeNotParsed());
        } catch (Exception ex) {
            log.error("MediaType {} could not be parsed, error {}", rawBody.getContentTypeNotParsed(), ex.getMessage());
            parsedMediaType = MediaType.parseMediaType(FALLBACK_MEDIA_TYPE);
        }
        parsedBody.setContentType(parsedMediaType);

        return parsedBody;
    }
}
