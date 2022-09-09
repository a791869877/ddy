package com.push.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@Data
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class MusicMessage extends ResponseMsgBody{
    @XStreamAlias("Music")
    private Music Music;

    public MusicMessage(Map<String ,String> map, Music music){
        super(map);
        this.setMsgType("Music");
        this.Music = music;
    }

    public MusicMessage(){

    }
}
