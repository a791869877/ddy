package com.push.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * 响应消息体
 */
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ResponseMsgBody {
    /**接收方帐号（收到的OpenID）*/
    @XStreamAlias("ToUserName")
    private String ToUserName;
    /** 开发者微信号 */
    @XStreamAlias("FromUserName")
    private String FromUserName;
    /** 消息创建时间 */
    @XStreamAlias("CreateTime")
    private long CreateTime;
    /** 消息类型*/
    @XStreamAlias("MsgType")
    private String MsgType;
    /** 文本消息的消息体 */
    @XStreamAlias("Content")
    private String Content;
    // setter/getter

    public ResponseMsgBody(Map<String ,String> map){
        this.FromUserName=map.get("ToUserName");
        this.ToUserName=map.get("FromUserName");
        this.CreateTime = System.currentTimeMillis()/1000;
    }
    public ResponseMsgBody(){

    }
}
