package com.sharefable.api.common.content;

import com.sharefable.api.common.Utils;
import com.sharefable.api.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.api.common.req.AssetContentBody;
import com.sharefable.api.common.req.ReqParamMissingException;
import org.springframework.http.MediaType;

public class ContentTypeParser {
    public static BaseAssetBodyParser parse(NewProxyAssetReqBodyParsed body) throws ReqParamMissingException {
        AssetContentBody respBody = body.getRespBody();
        if (respBody == null){
            throw new ReqParamMissingException("Required param body is missing for req");
        }

        ProxyAssetFileNameResolver fileNameResolver = new ProxyAssetFileNameResolver(body.getOrigin(), body.getUrl());
        return ContentTypeParser.parse(body.getContentType(), fileNameResolver, respBody);
    }

    public static BaseAssetBodyParser parse(MediaType contentType, FileNameResolver resolver, AssetContentBody body) {
        if(Utils.isHtml(contentType)) {
            return new HtmlParser(
                resolver,
                (String)body.getBody(),
                body.isBase64Encoded()
            );
        } else  if (!Utils.isText(contentType)){
            return new GenericBinaryAssetParser(
                resolver,
                (String)body.getBody(),
                body.isBase64Encoded()
            );
        } else {
            return  new GenericTextAssetParser(
                resolver,
                (String)body.getBody(),
                body.isBase64Encoded()
            );
        }
    }
}
