package com.sharefable.api.common.content;

import com.sharefable.api.common.Utils;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HtmlParser extends GenericTextAssetParser {

    @Data
    @Builder
    private static class TagOpConfig {
        enum OpType {Replace, ReplaceWhenEqual}

        private String lhs;
        private OpType op;
        private String rhs;
    }

    public static final String PROXY_PREFIX_ATTR_NAME = "data-fl-pxy-";

    private static final Map<String, TagOpConfig[]> PROXY_TAG_CONFIG;

    static {
        PROXY_TAG_CONFIG = new HashMap<>();

        // For script tag we just change the src attr -> PROXY_PREFIX_ATTR_NAME + attr
        TagOpConfig scriptTagConfig = TagOpConfig.builder()
            .lhs("src")
            .op(TagOpConfig.OpType.Replace)
            .build();
        PROXY_TAG_CONFIG.put("script", new TagOpConfig[]{scriptTagConfig});


        // For script tag we change the href attr -> PROXY_PREFIX_ATTR_NAME + href
        TagOpConfig linkTagConfigHref = TagOpConfig.builder()
            .lhs("href")
            .op(TagOpConfig.OpType.Replace)
            .build();
        // and we change the rel attr only when rel attr value is preload
        TagOpConfig linkTagConfigRel = TagOpConfig.builder()
            .lhs("rel")
            .op(TagOpConfig.OpType.ReplaceWhenEqual)
            .rhs("preload")
            .build();
        PROXY_TAG_CONFIG.put("link", new TagOpConfig[]{linkTagConfigHref, linkTagConfigRel});
    }

    // TODO based on env generate this script
    public static final String PROXY_SCRIPT_SRC = "http://localhost:8080/api/v1/asset/cmn/js/sw_installer.js";
     static final String PROXY_SCRIPT = "<script type=\"text/javascript\" src=\"" + PROXY_SCRIPT_SRC + "\"></script>";

    public HtmlParser(FileNameResolver fileNameResolver, String htmlStr, boolean isBase64Encoded) {
        super(fileNameResolver, htmlStr, isBase64Encoded);
        postProcess();
    }

    @Override
    public String fileName() {
        return fileNameResolver.getFileName() + ".html";
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    /*
     * Replace all the <script/> tag to <fab-proxy-script/> and link tag to <fab-proxy-link/>
     * Add a new script tag with the fable's proxy script.
     * This script in turns register all the worker / observer and once done change all
     * <fab-proxy-link/> to <script/> tag
     */
    private void postProcess() {
        String contentStr = new String(getContent(), StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(contentStr);

        for (String proxyableTagName : PROXY_TAG_CONFIG.keySet()) {
            Elements els = doc.getElementsByTag(proxyableTagName);
            for (Element el : els) {
                TagOpConfig[] tagOpConfigs = PROXY_TAG_CONFIG.get(el.tagName());
                for (TagOpConfig config : tagOpConfigs) {
                    String attrVal = el.attr(config.lhs);
                    if (!Utils.isStrEmpty(attrVal)) {
                        switch (config.op) {
                            case Replace:
                                el.attr(PROXY_PREFIX_ATTR_NAME + config.lhs, attrVal);
                                el.removeAttr(config.lhs);
                                break;
                            case ReplaceWhenEqual:
                                if (attrVal.equals(config.rhs == null ? "" : config.rhs)) {
                                    el.attr(PROXY_PREFIX_ATTR_NAME + config.lhs, attrVal);
                                    el.removeAttr(config.lhs);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }


        Elements body = doc.getElementsByTag("body");
        if (body.size() > 0){
            body.append(PROXY_SCRIPT);
        } else {
            Elements html = doc.getElementsByTag("html");
            html.append("<body>" + PROXY_SCRIPT + "</body>");
        }

        content = doc.html().getBytes(StandardCharsets.UTF_8);
    }
}
