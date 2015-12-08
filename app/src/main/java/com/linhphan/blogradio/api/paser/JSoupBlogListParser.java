package com.linhphan.blogradio.api.paser;

import com.linhphan.androidboilerplate.api.Parser.IParser;
import com.linhphan.blogradio.data.BlogRadioModel;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by linhphan on 12/8/15.
 */
public class JSoupBlogListParser implements IParser {
    @Override
    public Object parse(Object data) {
        ArrayList<BlogRadioModel> result = null;
        if (data instanceof Document){
            Document root = (Document) data;
            Element content = root.getElementById("left_content");

            String href;
            String fullTitle;
            String blogNumber;
            String blogTitle;
            String[] arr;
            BlogRadioModel model;

            //get featured blog
            Elements aTags = content.select("div.box_hotnhat > div.box_hotnhat_right1 > h2 > a");
            if (aTags.size() > 0) {
                Element aTag = aTags.get(0);
                href = aTag.attr("abs:href");
                fullTitle = aTag.attr("title");
                arr = fullTitle.trim().split(":");
                blogNumber = arr[0].trim();
                blogTitle = arr[1].trim();

                model = new BlogRadioModel(blogNumber, blogTitle, href);
                result = new ArrayList<>();
                result.add(model);
            }

            //get other blog
            aTags = content.select("div.box_trangtrong_left2 > h2 > a");
            for (Element a : aTags){
                href = a.attr("abs:href");
                fullTitle = a.attr("title");
                arr = fullTitle.trim().split(":");
                blogNumber = arr[0].trim();
                if (arr.length >= 2) {
                    blogTitle = arr[1].trim();
                }else{
                    blogTitle = "Unknown";
                }
                model = new BlogRadioModel(blogNumber, blogTitle, href);
                if (result == null){
                    result = new ArrayList<>();
                }
                result.add(model);

            }


            //get poster images
            if (result != null && result.size() > 0) {
                String posterImagePath = content.select("div.box_hotnhat > div.box_hotnhat_left > a > img").attr("abs:src");
                result.get(0).setPosterImagePath(posterImagePath);
                Elements imgTags = content.select("div > div.box_trangtrong_left1 > a > img");
                for (int i = 0; i < imgTags.size(); i++) {
                    posterImagePath = imgTags.get(i).attr("abs:src");
                    result.get(i + 1).setPosterImagePath(posterImagePath);
                }
            }
        }


        return result;
    }
}
