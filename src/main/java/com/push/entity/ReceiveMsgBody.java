package com.push.entity;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 接收消息实体
 */
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ReceiveMsgBody {
    /**开发者微信号*/
    private String ToUserName;
    /** 发送消息用户的openId */
    private String FromUserName;
    /** 消息创建时间 */
    private long CreateTime;
    /**消息类型*/
    private String MsgType;
    /** 消息ID，根据该字段来判重处理 */
    private long MsgId;
    /** 文本消息的消息体 */
    private String Content;

    private String MediaId;

    private String Event;

    //纬度
    private double Latitude;

    //经度
    private double Longitude;

    //地理位置精度
    private double Precision;

    //点击事件key
    private String EventKey;

    // setter/getter
}
