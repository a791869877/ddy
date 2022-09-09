package com.push.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
public class Music {
    /**
     * 音乐标题
     */
    @XStreamAlias("Title")
    private String title ;
    /**
     * 音乐描述
     */
    @XStreamAlias("Description")
    private String description;
    /**
     * 音乐连接
     */
    @XStreamAlias("MusicUrl")
    private String musicURL;
    /**
     * 高质量音乐链接，WIFI环境优先使用该链接播放音乐
     */
    @XStreamAlias("HQMusicUrl")
    private String hQMusicUrl;
    /**
     * 缩略图的媒体id，通过素材管理中的接口上传多媒体文件，得到的id
     */
    @XStreamAlias("ThumbMediaId")
    private String thumbMediaId;
}

