package com.sharefable.appserver.common.content;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Slf4j
public class HtmlParser extends GenericAssetParser {
    public static final String PROXY_PREFIX_TAG_NAME = "fab-proxy-";
    private static final String[] PROXY_TAG_NAMES = new String[]{"script", "link"};

    public static final String PROXY_SCRIPT_SRC = "https://cdn.sharefable.com/proxy_script.js";
    private static final String PROXY_SCRIPT = "<script type=\"text/javascript\" src=\"" + PROXY_SCRIPT_SRC + "\"></script>";

    public HtmlParser(FileNameResolver fileNameResolver, String htmlStr, boolean isBase64Encoded) {
        super(fileNameResolver, htmlStr, isBase64Encoded);
        postProcess();
    }

    @Override
    public String fileName() {
        return fileNameResolver.getFileName() + ".html";
    }

    @Override
    public String getContent() {
        return content;
    }

    /*
     * Replace all the <script/> tag to <fab-proxy-script/> and link tag to <fab-proxy-link/>
     * Add a new script tag with the fable's proxy script.
     * This script in turns register all the worker / observer and once done change all
     * <fab-proxy-link/> to <script/> tag
     */
    private void postProcess() {
        // TODO check with documents that are not proper html, if required raise exception
        Document doc = Jsoup.parse(getContent());

        boolean isTagReplaced = false;
        for (String proxyTagName : PROXY_TAG_NAMES) {
            Elements els = doc.getElementsByTag(proxyTagName);
            for (Element el : els) {
                isTagReplaced = true;
                el.tagName(PROXY_PREFIX_TAG_NAME + proxyTagName);
            }
        }

        if (isTagReplaced) {
            Elements heads = doc.getElementsByTag("head");
            if (heads.size() == 0) {
                Elements html = doc.getElementsByTag("html");
                if (html.size() != 0) {
                    Elements children = html.get(0).children();
                    if (children.size() != 0) {
                        children.get(0).before("<head>" + PROXY_SCRIPT + "</head>");
                    }
                    // Html tag must contain at least some other tag than head tag i.e. <body> tag
                }
                // If there is no html tag that means there is no content in html page
            } else {
                Elements children = heads.get(0).children();
                if (children.size() == 0){
                    heads.get(0).append(PROXY_SCRIPT);
                } else {
                    children.get(0).before(PROXY_SCRIPT);
                }
            }
        }

        content = doc.html();
    }
}
