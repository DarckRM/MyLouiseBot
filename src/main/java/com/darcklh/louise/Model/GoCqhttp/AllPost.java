package com.darcklh.louise.Model.GoCqhttp;

import lombok.Data;

/**
 * @author DarckLH
 * @date 2022/11/12 15:06
 * @Description 所有的 POST 上报都具有的字段
 */
public interface AllPost {


    public PostType getPost_type();
    public long getTime();
    public long getSelf_id();
    public void setPost_type(PostType post_type);
    public void setTime(long time);
    public void setSelf_id(long self_id);

    public enum PostType {
        none,
        message,
        request,
        notice,
        meta_event;
    }
}
