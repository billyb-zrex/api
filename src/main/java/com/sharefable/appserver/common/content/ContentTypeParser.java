package com.sharefable.appserver.common.content;

import com.sharefable.appserver.common.Utils;
import com.sharefable.appserver.common.req.NewProxyAssetReqBodyParsed;

public class ContentTypeParser {
    public static BaseParser parse(NewProxyAssetReqBodyParsed body) {
        if(Utils.isHtml(body.getContentType())) {
            return new HtmlParser(
                new FileNameResolver(body.getOrigin(), body.getUrl()),
                (String)body.getRespBody().getBody(),
                body.getRespBody().isBase64Encoded()
            );
        } else if (body.getRespBody() != null) {
            return  new GenericAssetParser(
                new FileNameResolver(body.getOrigin(), body.getUrl()),
                (String)body.getRespBody().getBody(),
                body.getRespBody().isBase64Encoded()
            );
        }
        return null;
    }
}
