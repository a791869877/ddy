package com.util;

import com.push.entity.Weather;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpTemplateMsgService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author cVzhanshi
 * @create 2022-08-04 21:09
 */
@Component
public class Pusher {

    @Autowired
    private  RedisUtil redisUtil;

    private static String appId = "wx80da8c15fb1a6558";
    private static String secret = "6c860bf822583ff6790bfa0f6a386438";



    public void push(String userid){
        //1，配置
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        wxStorage.setAppId(appId);
        wxStorage.setSecret(secret);
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);
        //2,推送消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                //mine
                .toUser(userid)
                .templateId("-Z0mUb6yJeA3xM7Nkq1r99Tf4qLRhogbgNKztdN-7js")
                .build();
        //3,如果是正式版发送模版消息，这里需要配置你的信息
        String citycode = (String) redisUtil.hget("user_location", userid);
        if (citycode==null){
            WxMpKefuMessage message=new WxMpKefuMessage();
            message=WxMpKefuMessage.TEXT().toUser(userid).content("获取定位~~~").build();
            try {
                wxMpService.getKefuService().sendKefuMessage(message);
            } catch (WxErrorException e) {
                e.printStackTrace();
            }
        }
        Weather weather = BaiduMapGeocoderUtil.getWeather(citycode);
        templateMessage.addData(new WxMpTemplateData("riqi",weather.getDate() + "  "+ weather.getWeek(),"#00BFFF"));
        templateMessage.addData(new WxMpTemplateData("tianqi",weather.getText_now(),"#00FFFF"));
        templateMessage.addData(new WxMpTemplateData("low",weather.getLow() + "","#173177"));
        templateMessage.addData(new WxMpTemplateData("temp",weather.getTemp() + "","#EE212D"));
        templateMessage.addData(new WxMpTemplateData("high",weather.getHigh()+ "","#FF6347" ));
        templateMessage.addData(new WxMpTemplateData("windclass",weather.getWind_class()+ "","#42B857" ));
        templateMessage.addData(new WxMpTemplateData("winddir",weather.getWind_dir()+ "","#B95EA3" ));
        templateMessage.addData(new WxMpTemplateData("caihongpi",CaiHongPiUtils.getCaiHongPi(),"#FF69B4"));
//        templateMessage.addData(new WxMpTemplateData("lianai",JiNianRiUtils.getLianAi()+"","#FF1493"));
//        templateMessage.addData(new WxMpTemplateData("shengri1",JiNianRiUtils.getBirthday_Jo()+"","#FFA500"));
//        templateMessage.addData(new WxMpTemplateData("shengri2",JiNianRiUtils.getBirthday_Hui()+"","#FFA500"));
//        templateMessage.addData(new WxMpTemplateData("en",map.get("en") +"","#C71585"));
//        templateMessage.addData(new WxMpTemplateData("zh",map.get("zh") +"","#C71585"));
//        String beizhu = "❤";
//        if(JiNianRiUtils.getLianAi() % 365 == 0){
//            beizhu = "今天是恋爱" + (JiNianRiUtils.getLianAi() / 365) + "周年纪念日！";
//        }
//        if(JiNianRiUtils.getBirthday_Jo()  == 0){
//            beizhu = "今天是生日，生日快乐呀！";
//        }
//        if(JiNianRiUtils.getBirthday_Hui()  == 0){
//            beizhu = "今天是生日，生日快乐呀！";
//        }
//        templateMessage.addData(new WxMpTemplateData("beizhu",beizhu,"#FF0000"));

        try {
            System.out.println(templateMessage.toJson());
            WxMpTemplateMsgService templateMsgService = wxMpService.getTemplateMsgService();
            System.out.println("获取到客服能力");
            String s = templateMsgService.sendTemplateMsg(templateMessage);
//            System.out.println("发送消息");
//            System.out.println(wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage));

        } catch (Exception e) {
            System.out.println("推送失败：" + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void send(){
        //1，配置
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        wxStorage.setAppId(appId);
        wxStorage.setSecret(secret);
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);
        WxMpKefuMessage message=WxMpKefuMessage.TEXT().toUser("o1nNU6u9y3KpfOs01UrJU4fOd6vo").content("你好啊聪哥").build();
        try {
            System.out.println(wxMpService.getKefuService().sendKefuMessage(message));

        } catch (Exception e) {
            System.out.println("推送失败：" + e.getMessage());
            e.printStackTrace();
        }
    }


}

