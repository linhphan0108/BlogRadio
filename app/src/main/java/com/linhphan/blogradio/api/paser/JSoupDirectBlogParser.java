package com.linhphan.blogradio.api.paser;

import com.linhphan.androidboilerplate.api.Parser.IParser;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by linhphan on 12/7/15.
 */
public class JSoupDirectBlogParser implements IParser {
    @Override
    public Object parse(Object data) {
        String url = null;
        if (data instanceof Document){
            Document root = (Document) data;
            Elements elements = root.select(".player source");
            if (elements != null && elements.size() > 0){
                url = elements.get(0).attr("abs:src");
            }
        }
        return url;
    }
}
