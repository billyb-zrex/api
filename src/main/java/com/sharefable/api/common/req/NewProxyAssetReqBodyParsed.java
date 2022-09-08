package com.sharefable.api.common.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, String> queryParams;
    private Map<String, Object> meta;

    public static final String FALLBACK_MEDIA_TYPE = "fable/fallback-mime";

    public static NewProxyAssetReqBodyParsed from(NewProxyAssetReqBody rawBody) throws MalformedURLException {
        NewProxyAssetReqBodyParsed parsedBody = new NewProxyAssetReqBodyParsed();

        parsedBody.setMethod(rawBody.getMethod());
        parsedBody.setRespBody(rawBody.getRespBody());
        parsedBody.setOrigin(new URL(rawBody.getOriginNotParsed()));
        parsedBody.setUrl(new URL(rawBody.getUrlNotParsed()));
        parsedBody.setReqHeaders(rawBody.getReqHeaders());
        parsedBody.setRespHeaders(rawBody.getRespHeaders());

        int statusRaw = rawBody.getStatusNotParsed();
        // We would ideally not send 304 while serving a request
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/304
        statusRaw = statusRaw == 304 ? 200 : statusRaw;
        HttpStatus status = HttpStatus.resolve(statusRaw);
        parsedBody.setStatus(status);

        MediaType parsedMediaType;
        try {
           parsedMediaType = MediaType.parseMediaType(rawBody.getContentTypeNotParsed());
        } catch (Exception ex) {
            log.error("MediaType {} could not be parsed, error {}", rawBody.getContentTypeNotParsed(), ex.getMessage());
            parsedMediaType = MediaType.parseMediaType(FALLBACK_MEDIA_TYPE);
        }
        parsedBody.setContentType(parsedMediaType);

        try {
            parsedBody.setQueryParams(
                UriComponentsBuilder.fromUri(parsedBody.getUrl().toURI()).build().getQueryParams().toSingleValueMap()
            );
        } catch (URISyntaxException e) {
            log.error("Error while extracting query parameters from proxy url link. Msg: {}", e.getMessage());
            parsedBody.setQueryParams(new HashMap<>());
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("origUrl", rawBody.getUrlNotParsed());
        meta.put("origOrigin", rawBody.getOriginNotParsed());
        parsedBody.setMeta(meta);

        return parsedBody;
    }
}
