package com.linhphan.blogradio.data;

/**
 * Created by linhphan on 12/8/15.
 */
public class BlogRadioModel {
    private String mBlogNumber;
    private String mBlogTitle;
    private String mDetailPath;
    private String mPosterImagePath;

    public BlogRadioModel(String blogNumber, String logTitle, String directPath) {
        this.mBlogNumber = blogNumber;
        this.mBlogTitle = logTitle;
        this.mDetailPath = directPath;
    }

    public BlogRadioModel(String blogNumber, String logTitle, String directPath, String posterImagePath) {
        this.mBlogNumber = blogNumber;
        this.mBlogTitle = logTitle;
        this.mDetailPath = directPath;
        this.mPosterImagePath = posterImagePath;
    }

    public void setBlogNumber(String blogNumber){
        this.mBlogNumber = blogNumber;
    }

    public String getBlogNumber(){
        return this.mBlogNumber;
    }

    public void setBlogTitle(String blogTitle){
        this.mBlogTitle = blogTitle;
    }

    public String getBlogTitle(){
        return this.mBlogTitle;
    }

    public void setDetailPath(String path){
        this.mDetailPath = path;
    }

    public String getDetailPath(){
        return this.mDetailPath;
    }

    public void setPosterImagePath(String path){
        this.mPosterImagePath = path;
    }

    public String getPosterImagePath(){
        return this.mPosterImagePath;
    }
}
