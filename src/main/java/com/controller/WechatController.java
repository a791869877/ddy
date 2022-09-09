package com.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.push.entity.*;
import com.util.BaiduMapGeocoderUtil;
import com.util.Pusher;
import com.util.RedisUtil;
import com.util.WechatUtils;
import com.websocket.test.util.HttpClientUtil;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@RestController
public class WechatController {
    /** 日志 */
    private Logger logger = LoggerFactory.getLogger(getClass());
    /** 工具类 */
    @Autowired
    private WechatUtils wechatUtils;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Pusher pusher;

    private String url="https://api.weixin.qq.com/cgi-bin/openapi/quota/get?access_token=";


    /**
     * 微信公众号接口配置验证
     * @return
     */
    @RequestMapping(value = "/wechat", method = RequestMethod.GET,produces = "text/plain;charset=utf-8")
    public String checkSignature(String signature, String timestamp,
                                 String nonce, String echostr) {
        logger.info("signature = {}", signature);
        logger.info("timestamp = {}", timestamp);
        logger.info("nonce = {}", nonce);
        logger.info("echostr = {}", echostr);
        // 第一步：自然排序
        String[] tmp = {wechatUtils.getToken(), timestamp, nonce};
        Arrays.sort(tmp);
        // 第二步：sha1 加密
        String sourceStr = StringUtils.join(tmp);
        String localSignature = DigestUtils.sha1Hex(sourceStr);
        // 第三步：验证签名
        if (signature.equals(localSignature)) {
            return echostr;
        }
        return null;
    }

    @RequestMapping("/getAccessToken")
    public String getAccessToken() {
        try {
            String accessToken = wechatUtils.getAccessToken();
            logger.info("access_token = {}", accessToken);
            return accessToken;
        } catch (WxErrorException e) {
            logger.error("获取access_token失败。", e);
        }
        return null;
    }

    /**
     * 接收用户消息
     * @param receiveMsgBody 消息
     * @return
     */
    @RequestMapping(value = "/wechat1", produces = {"application/xml; charset=UTF-8"})
    @ResponseBody
    public ResponseMsgBody getUserMessage(@RequestBody ReceiveMsgBody receiveMsgBody) throws WxErrorException, IOException {
        logger.info("接收到的消息：{}", receiveMsgBody);
        MsgType msgType = MsgType.getMsgType(receiveMsgBody.getMsgType());
        String fromUserName = receiveMsgBody.getFromUserName();
        switch (msgType) {
            case text:
                String msg=" ";
                logger.info("接收到的消息类型为{}", MsgType.text.getMsgType());
                ResponseMsgBody responseMsgBody=new ResponseMsgBody();
                responseMsgBody.setMsgType("text");
                responseMsgBody.setFromUserName("gh_f9be6a2f0bdf");
                responseMsgBody.setToUserName(receiveMsgBody.getFromUserName());
                responseMsgBody.setCreateTime(System.currentTimeMillis());
                if (receiveMsgBody.getContent().contains("点歌")){
                    Music music=new Music();
                    music.setTitle("test");
                    music.setDescription("meiyou gequ ");
                    music.setMusicURL("https://y.qq.com/n/ryqq/player.mp3");
                    MusicMessage message=new MusicMessage();
                    message.setMusic(music);
                    message.setFromUserName(receiveMsgBody.getToUserName());
                    message.setToUserName(receiveMsgBody.getFromUserName());
                    message.setCreateTime(System.currentTimeMillis());
                    //自动点歌
                    return message;

                }
                if (receiveMsgBody.getContent().contains("次数")){
                    JSONObject jsonObject = wechatUtils.materialCount("1");
                    Integer imageCount = jsonObject.getInteger("imageCount");
                    Integer videoCount = jsonObject.getInteger("videoCount");
                    Integer voiceCount = jsonObject.getInteger("voiceCount");
                    Integer newsCount = jsonObject.getInteger("newsCount");
                    msg="图片数量: "+imageCount+" 视频数量: "+videoCount+" 语音数量: "+voiceCount+" 新闻数量: "+newsCount;
                    responseMsgBody.setContent(msg);
                    return responseMsgBody;

                }
                if (receiveMsgBody.getContent().contains("素材")){
                    String accessToken = wechatUtils.getAccessToken();
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("accessToken",accessToken);
                    jsonObject.put("cgi_path","/cgi-bin/material/add_material");
                    String http = httpClientUtil.postHttpJ(url + accessToken,jsonObject.toJSONString());
                    msg="素材次数:"+http;
                    responseMsgBody.setContent(msg);
                    return responseMsgBody;
                }
                if (receiveMsgBody.getContent().contains("token")){
                    String accessToken = wechatUtils.getAccessToken();
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("accessToken",accessToken);
                    jsonObject.put("cgi_path","/cgi-bin/token");
                    String http = httpClientUtil.postHttpJ(url + accessToken,jsonObject.toJSONString());
                    msg="token:"+http;
                    responseMsgBody.setContent(msg);
                    return responseMsgBody;
                }
                if (receiveMsgBody.getContent().equals("重置")){
                    redisUtil.del("token");
                    responseMsgBody.setContent("重置完成");
                    return responseMsgBody;
                }

                if (receiveMsgBody.getContent().equals("清除")){

                    wechatUtils.clearQuota(wechatUtils.getAppid());
                    responseMsgBody.setContent("清理完毕");
                    return responseMsgBody;

                }
                break;
            case image:
                String mediaId = receiveMsgBody.getMediaId();
                logger.info("接收到的消息类型为{}", MsgType.image.getMsgType());
                ResponseImageMsg imageMsg = new ResponseImageMsg();
                imageMsg.setToUserName(receiveMsgBody.getFromUserName());
                imageMsg.setFromUserName(receiveMsgBody.getToUserName());
                imageMsg.setCreateTime(System.currentTimeMillis());
                imageMsg.setMsgType(MsgType.image.getMsgType());
                imageMsg.setMediaId(new String[]{receiveMsgBody.getMediaId()});
                return imageMsg;

//                //下载图片素材
//                File file = wechatUtils.DownloadMaterial(receiveMsgBody.getMediaId());
//                FileInputStream fileInputStream=null;
//                try {
//                    fileInputStream= new FileInputStream(file);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                WxMpMaterialUploadResult image = wechatUtils.uploadFilesToWeChat("image", "jpg", mediaId, fileInputStream);
////                WxMediaImgUploadResult uploadimg = wechatUtils.uploadimg(file);
//
//                wechatUtils.send("image",receiveMsgBody.getFromUserName(),image.getMediaId());

            case voice:
                String voiceid = receiveMsgBody.getMediaId();
                logger.info("接收到的消息类型为{}", MsgType.voice.getMsgType());
                ResponseVoiceMsg voiceMsg = new ResponseVoiceMsg();
                voiceMsg.setToUserName(receiveMsgBody.getFromUserName());
                voiceMsg.setFromUserName(receiveMsgBody.getToUserName());
                voiceMsg.setCreateTime(System.currentTimeMillis());
                voiceMsg.setMsgType(MsgType.voice.getMsgType());
                voiceMsg.setMediaId(new String[]{receiveMsgBody.getMediaId()});
                return voiceMsg;
//                File voice = wechatUtils.DownloadMaterial(voiceid);
//                FileInputStream stream=null;
//                try {
//                    stream= new FileInputStream(voice);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                WxMpMaterialUploadResult myvoice = wechatUtils.uploadFilesToWeChat("voice", "amr", voiceid, stream);
//                wechatUtils.send("voice",receiveMsgBody.getFromUserName(),myvoice.getMediaId());
//                break;
            case video:
                break;
            case shortvideo:
                break;
            case location:
                break;
            case link:
                break;
            case music:
                break;
            case news:
                break;
            case event:
                String event = receiveMsgBody.getEvent();
                if (event.equals("unsubscribe")){
                    //取消订阅
                    wechatUtils.send("text","o1nNU6ltheCjccM1guxMHrznHQP4","有个憨批取关了");
                }else if (event.equals("subscribe")){
                    //订阅
                    pusher.push(receiveMsgBody.getFromUserName());

                }else if (event.equals("LOCATION")){
                    //坐标
                    double latitude = receiveMsgBody.getLatitude();
                    double longitude = receiveMsgBody.getLongitude();
                    JSONObject addressInfoByLngAndLat = BaiduMapGeocoderUtil.getAddressInfoByLngAndLat(longitude + "", latitude + "");
                    if (addressInfoByLngAndLat.getInteger("status").equals(0)){
                        String citycode = addressInfoByLngAndLat.getJSONObject("result").getJSONObject("addressComponent").getString("adcode");
                        redisUtil.hset("user_location",fromUserName,citycode);
                    }
                }
            default:
                // 其他类型
                break;
        }
        return null;
    }

    // 早上8点定时提醒天气
    @Scheduled(cron = "0 0 8 * * ? ")
    @RequestMapping(value = "/goodmorning", produces = {"application/xml; charset=UTF-8"})
    public String goodMorning(){
        String accessToken = getAccessToken();
        String url="https://api.weixin.qq.com/cgi-bin/user/get?access_token="+accessToken;
        String responseMsg = httpClientUtil.getHttp(url);
        JSONObject jsonObject = JSONObject.parseObject(responseMsg);
        if (jsonObject.containsKey("errcode")&&(jsonObject.getString("errcode").equals("42001")||jsonObject.getString("errcode").equals("40001"))){
            redisUtil.del("token");
            accessToken=getAccessToken();
            url="https://api.weixin.qq.com/cgi-bin/user/get?access_token="+accessToken;
            responseMsg = httpClientUtil.getHttp(url);
            jsonObject = JSONObject.parseObject(responseMsg);
        }
        List<String> strings = jsonObject.getJSONObject("data").getJSONArray("openid").toJavaList(String.class);
        for (String string : strings) {
            pusher.push(string);
        }
        return null;
    }

    public static String getMenuStr() throws JSONException {
        JSONObject firstLevelMenu = new JSONObject();//一级菜单
        JSONArray firstLevelMenuArray = new JSONArray();//一级菜单列表
        //一级菜单内容1 音乐
        JSONObject firstLevelMenuContext1 = new JSONObject();
        JSONArray firstLevelMenuContext1Array = new JSONArray();
        //二级菜单 网易云 QQ音乐
        JSONObject music1=new JSONObject();
        music1.put("type", "view");
        music1.put("name", "网易云");
        music1.put("key", "V1001_MUSIC_WYY");
        music1.put("url","https://music.163.com/");
        JSONObject music2=new JSONObject();
        music2.put("type", "view");
        music2.put("name", "QQ音乐");
        music2.put("key", "V1001_MUSIC_QQ");
        music2.put("url","https://y.qq.com");
        firstLevelMenuContext1Array.add(music1);
        firstLevelMenuContext1Array.add(music2);
        firstLevelMenuContext1.put("sub_button",firstLevelMenuContext1Array);
        firstLevelMenuContext1.put("name", "音乐");
        //以及菜单内容2 电影
        JSONObject firstLevelMenuContext2 = new JSONObject();
        JSONArray firstLevelMenuContext2Array = new JSONArray();
        //一级菜单内容2的二级菜单内容1
        JSONObject movies1 = new JSONObject();
        movies1.put("type", "view");
        movies1.put("name", "腾讯视频");
        movies1.put("key", "V1002_TODAY_QQ");
        movies1.put("url","https://v.qq.com/");
        //一级菜单内容2的二级菜单内容2
        JSONObject movies2 = new JSONObject();
        movies2.put("type", "view");
        movies2.put("name", "爱奇艺");
        movies2.put("key", "V1002_TODAY_AQY");
        movies2.put("url", "http://www.iqiyi.com");
        firstLevelMenuContext2Array.add(movies1);
        firstLevelMenuContext2Array.add(movies2);
        firstLevelMenuContext2.put("name", "电影");
        firstLevelMenuContext2.put("sub_button", firstLevelMenuContext2Array);
        firstLevelMenuArray.add(firstLevelMenuContext1);
        firstLevelMenuArray.add(firstLevelMenuContext2);
        firstLevelMenu.put("button", firstLevelMenuArray);
        return firstLevelMenu.toString();
    }


    public static String getspectial() throws JSONException {
        JSONObject firstLevelMenu = new JSONObject();//一级菜单
        JSONArray firstLevelMenuArray = new JSONArray();//一级菜单列表
        //一级菜单内容1 音乐
        JSONObject firstLevelMenuContext1 = new JSONObject();
        JSONArray firstLevelMenuContext1Array = new JSONArray();
        //二级菜单 网易云 QQ音乐
        JSONObject music1=new JSONObject();
        music1.put("type", "view");
        music1.put("name", "网易云");
        music1.put("key", "V1001_MUSIC_WYY_test");
        music1.put("url","https://music.163.com/");
        JSONObject music2=new JSONObject();
        music2.put("type", "view");
        music2.put("name", "QQ音乐");
        music2.put("key", "V1001_MUSIC_QQ_test");
        music2.put("url","https://y.qq.com");
        firstLevelMenuContext1Array.add(music1);
        firstLevelMenuContext1Array.add(music2);
        firstLevelMenuContext1.put("sub_button",firstLevelMenuContext1Array);
        firstLevelMenuContext1.put("name", "音乐");
        //以及菜单内容2 电影
        JSONObject firstLevelMenuContext2 = new JSONObject();
        JSONArray firstLevelMenuContext2Array = new JSONArray();
        //一级菜单内容2的二级菜单内容1
        JSONObject movies1 = new JSONObject();
        movies1.put("type", "view");
        movies1.put("name", "腾讯视频");
        movies1.put("key", "V1002_TODAY_QQ_test");
        movies1.put("url","https://v.qq.com/");
        //一级菜单内容2的二级菜单内容2
        JSONObject movies2 = new JSONObject();
        movies2.put("type", "view");
        movies2.put("name", "爱奇艺");
        movies2.put("key", "V1002_TODAY_AQY_test");
        movies2.put("url", "http://www.iqiyi.com");
        firstLevelMenuContext2Array.add(movies1);
        firstLevelMenuContext2Array.add(movies2);
        firstLevelMenuContext2.put("name", "电影");
        firstLevelMenuContext2.put("sub_button", firstLevelMenuContext2Array);
        firstLevelMenuArray.add(firstLevelMenuContext1);
        firstLevelMenuArray.add(firstLevelMenuContext2);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("tag_id","101");
        firstLevelMenu.put("button", firstLevelMenuArray);
        firstLevelMenu.put("matchrule", jsonObject);
        return firstLevelMenu.toString();
    }

    //创建个性化菜单
    @RequestMapping(value = "/creatSpecialMenu", produces = {"application/xml; charset=UTF-8"})
    public void creatSpecialMenu() throws Exception{
        String custmMenuUrl = "https://api.weixin.qq.com/cgi-bin/menu/addconditional?access_token=";

        //获取access_token
        String accessToken = getAccessToken();
        custmMenuUrl = custmMenuUrl + accessToken;

        URL url = new URL(custmMenuUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(getspectial().getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = connection.getInputStream();
        int size =inputStream.available();
        byte[] bs =new byte[size];
        inputStream.read(bs);
        String message=new String(bs,"UTF-8");

        System.out.println(message);
    }

    //创建菜单接口
    @RequestMapping(value = "/creatMenu", produces = {"application/xml; charset=UTF-8"})
    public  void createCustomMenu() throws Exception{
        String custmMenuUrl = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=";

        //获取access_token
        String accessToken = getAccessToken();
        custmMenuUrl = custmMenuUrl + accessToken;

        URL url = new URL(custmMenuUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(getMenuStr().getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = connection.getInputStream();
        int size =inputStream.available();
        byte[] bs =new byte[size];
        inputStream.read(bs);
        String message=new String(bs,"UTF-8");

        System.out.println(message);
    }


    //创建菜单接口
    @RequestMapping(value = "/creatMenutest", produces = {"application/xml; charset=UTF-8"})
    public  void createCustomMenutest() throws Exception{
        String custmMenuUrl = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=";
        //获取access_token
        String accessToken = getAccessToken();
        custmMenuUrl = custmMenuUrl + accessToken;
        String json="{\n" +
                "    \"button\": [\n" +
                "        {\n" +
                "            \"type\": \"click\", \n" +
                "            \"name\": \"瓜皮1号\", \n" +
                "            \"media_id\": \"123\", \n" +
                "            \"key\": \"test1\"\n" +
                "        }, \n" +
                "        {\n" +
                "            \"type\": \"click\", \n" +
                "            \"name\": \"我是瓜皮\", \n" +
                "            \"media_id\": \"345\", \n" +
                "            \"key\": \"test2\"\n" +
                "        }, \n" +
                "        {\n" +
                "            \"name\": \"瓜皮集合\", \n" +
                "            \"sub_button\": [\n" +
                "                {\n" +
                "                    \"type\": \"view\", \n" +
                "                    \"name\": \"搜1\", \n" +
                "                    \"url\": \"http://www.soso.com/\"\n" +
                "                }, \n" +
                "                {\n" +
                "                    \"type\": \"view\", \n" +
                "                    \"name\": \"视2\", \n" +
                "                    \"url\": \"http://v.qq.com/\"\n" +
                "                }, \n" +
                "                {\n" +
                "                    \"type\": \"click\", \n" +
                "                    \"name\": \"赞3\", \n" +
                "                    \"key\": \"V1001_GOOD\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}\n";
        String s = httpClientUtil.postHttpJ(custmMenuUrl, json);
        System.out.println(s);
    }

    // 每天中午十二点定时推送新闻
    @Scheduled(cron = "0 51 11 * * ? ")
    @RequestMapping(value = "/eat", produces = {"application/xml; charset=UTF-8"})
    public String eat() throws WxErrorException {
        String accessToken = getAccessToken();
        String url="https://api.weixin.qq.com/cgi-bin/user/get?access_token="+accessToken;
        String responseMsg = httpClientUtil.getHttp(url);
        JSONObject jsonObject = JSONObject.parseObject(responseMsg);
        if (jsonObject.containsKey("errcode")&&(jsonObject.getString("errcode").equals("42001")||jsonObject.getString("errcode").equals("40001"))){
            redisUtil.del("token");
            accessToken=getAccessToken();
            url="https://api.weixin.qq.com/cgi-bin/user/get?access_token="+accessToken;
            responseMsg = httpClientUtil.getHttp(url);
            jsonObject = JSONObject.parseObject(responseMsg);
        }
        List<String> strings = jsonObject.getJSONObject("data").getJSONArray("openid").toJavaList(String.class);
        for (String string : strings) {
            if (string.equals("o1nNU6ltheCjccM1guxMHrznHQP4")){
                WxMpKefuMessage message=WxMpKefuMessage.TEXT().toUser("o1nNU6ltheCjccM1guxMHrznHQP4").content("该吃饭了").build();
                WxMpService wxMpService=new WxMpServiceImpl();
                wxMpService.getKefuService().sendKefuMessage(message);
            }

        }
        return null;
    }



    //创建标签
    @RequestMapping(value = "/createtag", produces = {"application/xml; charset=UTF-8"})
    public String createtag(){
        String accessToken = getAccessToken();
        String url="https://api.weixin.qq.com/cgi-bin/tags/create?access_token="+accessToken;
        JSONObject jsonObject=new JSONObject();
        JSONObject object=new JSONObject();
        object.put("name","other");
        jsonObject.put("tag",object);
        String s = httpClientUtil.postHttpJ(url, jsonObject.toJSONString());
        System.out.println(s);
        return null;
    }

    //创建标签
    @RequestMapping(value = "/bind", produces = {"application/xml; charset=UTF-8"})
    public String bind(){
        String accessToken = getAccessToken();
        String url="https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token="+accessToken;
        JSONObject jsonObject=new JSONObject();
        JSONArray jsonArray=new JSONArray();
        jsonArray.add("o1nNU6ltheCjccM1guxMHrznHQP4");
        jsonObject.put("tagid",100);
        jsonObject.put("openid_list",jsonArray);
        String s = httpClientUtil.postHttpJ(url, jsonObject.toJSONString());
        System.out.println(s);
        return null;
    }

    //测试匹配
    @RequestMapping(value = "/trymatch", produces = {"application/xml; charset=UTF-8"})
    public String trymatch(String userid){
        String accessToken = getAccessToken();
        String url="https://api.weixin.qq.com/cgi-bin/menu/trymatch?access_token="+accessToken;
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("user_id",userid);
        String s = httpClientUtil.postHttpJ(url, jsonObject.toJSONString());
        System.out.println(s);
        return null;
    }

    //获取所有菜单
    //测试匹配
    @RequestMapping(value = "/getMenu", produces = {"application/xml; charset=UTF-8"})
    public String getMenu( ){
        String accessToken = getAccessToken();
        String url="https://api.weixin.qq.com/cgi-bin/get_current_selfmenu_info?access_token="+accessToken;
        String s = httpClientUtil.getHttp(url);
        JSONObject jsonObject = JSONObject.parseObject(s);
        System.out.println(s);
        return null;
    }

    //获取标签下的用户
    @RequestMapping(value = "/getuser", produces = {"application/xml; charset=UTF-8"})
    public String getuser(int tag){
        String accessToken = getAccessToken();
        String url="https://api.weixin.qq.com/cgi-bin/user/tag/get?access_token="+accessToken;
        JSONObject jsonObject1=new JSONObject();
        jsonObject1.put("tagid",tag);
        jsonObject1.put("next_openid",null);
        String s = httpClientUtil.postHttpJ(url, jsonObject1.toJSONString());
        JSONObject jsonObject = JSONObject.parseObject(s);
        System.out.println(s);
        return null;
    }


}