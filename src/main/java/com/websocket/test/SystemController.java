package com.websocket.test;

import com.alibaba.fastjson.JSONObject;
import com.websocket.test.util.HttpClientUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/system")
public class SystemController {


    private HttpClientUtil httpClientUtil;
    //页面请求
    @GetMapping("/index/{userId}")
    public ModelAndView socket(@PathVariable String userId) {
        ModelAndView mav = new ModelAndView("/socket1");
        mav.addObject("userId", userId);
        return mav;
    }

    //推送数据接口
    @ResponseBody
    @RequestMapping("/socket/push/{cid}")
    public Map pushToWeb(@PathVariable String cid, String message) {
        Map result = new HashMap();
        try {
            WebSocketServer.sendInfo(message,cid);
            result.put("code", 200);
            result.put("msg", "success");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @ResponseBody
    @PostMapping (value ="/test",produces = "application/json;charset=UTF-8")
    public String haha( @RequestBody JSONObject jsonParam){
        Integer event = jsonParam.getInteger("event");
        JSONObject data = jsonParam.getJSONObject("data");
        if (event.equals(10014)){
            //上下线
            if (data.getInteger("type").equals(1)){

            }

        }else if (event.equals(10008)){
            //收到群聊消息
        }else if (event.equals(10009)){
            //收到私聊

        }else if (event.equals(10010)){
            //自己发出消息
        }else if (event.equals(10006)){
            //转账时间
        }else if (event.equals(10013)){
            //撤回
        }else if (event.equals(10011)){
            //好友请求
        }else if (event.equals(10007)){
            //支付事件
        }

        System.out.println("123123");
        return null;
    }
}