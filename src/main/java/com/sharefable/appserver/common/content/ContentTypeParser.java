package com.sharefable.appserver.common.content;

import com.sharefable.appserver.common.Utils;
import com.sharefable.appserver.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.appserver.common.req.ReqParamMissingException;

public class ContentTypeParser {
    public static BaseAssetBodyParser parse(NewProxyAssetReqBodyParsed body) throws ReqParamMissingException {
        if (body.getRespBody() == null){
            throw new ReqParamMissingException("Required param body is missing for req");
        }

        if(Utils.isHtml(body.getContentType())) {
            return new HtmlParser(
                new FileNameResolver(body.getOrigin(), body.getUrl()),
                (String)body.getRespBody().getBody(),
                body.getRespBody().isBase64Encoded()
            );
        } else  {
            return  new GenericAssetParser(
                new FileNameResolver(body.getOrigin(), body.getUrl()),
                (String)body.getRespBody().getBody(),
                body.getRespBody().isBase64Encoded()
            );
        }
    }
}
