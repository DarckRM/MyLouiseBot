package com.darcklh.louise.Model.Louise;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

public class BooruImages {
    @TableId(type = IdType.AUTO)
    private int id;
    private String tags;
    private int recorded_at;
    private int created_at;
    private int updated_at;
    private int creator_id;
    private String author;
    private String source;
    private String md5;
    private String file_ext;
    private String file_url;
    private String preview_url;
    private String sample_url;
    private String jpeg_url;
    private String rating;
    private int file_size;
    private int sample_file_size;
    private int jpeg_file_size;
    private int parent_id;
    private int width;
    private int height;

    public BooruImages(int id, String tags, int recorded_at, int created_at, int updated_at, int creator_id, String author, String source, String md5, String file_ext, String file_url, String preview_url, String sample_url, String jpeg_url, String rating, int file_size, int sample_file_size, int jpeg_file_size, int parent_id, int width, int height) {
        this.id = id;
        this.tags = tags;
        this.recorded_at = recorded_at;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.creator_id = creator_id;
        this.author = author;
        this.source = source;
        this.md5 = md5;
        this.file_ext = file_ext;
        this.file_url = file_url;
        this.preview_url = preview_url;
        this.sample_url = sample_url;
        this.jpeg_url = jpeg_url;
        this.rating = rating;
        this.file_size = file_size;
        this.sample_file_size = sample_file_size;
        this.jpeg_file_size = jpeg_file_size;
        this.parent_id = parent_id;
        this.width = width;
        this.height = height;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setRecorded_at(int recorded_at) {
        this.recorded_at = recorded_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(int updated_at) {
        this.updated_at = updated_at;
    }

    public void setCreator_id(int creator_id) {
        this.creator_id = creator_id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setFile_ext(String file_ext) {
        this.file_ext = file_ext;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public void setSample_url(String sample_url) {
        this.sample_url = sample_url;
    }

    public void setJpeg_url(String jpeg_url) {
        this.jpeg_url = jpeg_url;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setFile_size(int file_size) {
        this.file_size = file_size;
    }

    public void setSample_file_size(int sample_file_size) {
        this.sample_file_size = sample_file_size;
    }

    public void setJpeg_file_size(int jpeg_file_size) {
        this.jpeg_file_size = jpeg_file_size;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getId() {
        return id;
    }

    public String getTags() {
        return tags;
    }

    public int getRecorded_at() {
        return recorded_at;
    }

    public int getCreated_at() {
        return created_at;
    }

    public int getUpdated_at() {
        return updated_at;
    }

    public int getCreator_id() {
        return creator_id;
    }

    public String getAuthor() {
        return author;
    }

    public String getSource() {
        return source;
    }

    public String getMd5() {
        return md5;
    }

    public String getFile_ext() {
        return file_ext;
    }

    public String getFile_url() {
        return file_url;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public String getSample_url() {
        return sample_url;
    }

    public String getJpeg_url() {
        return jpeg_url;
    }

    public String getRating() {
        return rating;
    }

    public int getFile_size() {
        return file_size;
    }

    public int getSample_file_size() {
        return sample_file_size;
    }

    public int getJpeg_file_size() {
        return jpeg_file_size;
    }

    public int getParent_id() {
        return parent_id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
