package com.websocket.test;

import org.apache.catalina.session.StandardSessionFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * 开启WebSocket支持
 * Created by huiyunfei on 2019/5/31.
 */
@Configuration
public class WebSocketConfig extends ServerEndpointConfig.Configurator {

    @Override
    /**
     * 修改握手信息
     */
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        StandardSessionFacade ssf = (StandardSessionFacade) request.getHttpSession();
        if (ssf != null) {
            HttpSession httpSession = (HttpSession) request.getHttpSession();
            //关键操作
            sec.getUserProperties().put("sessionId", httpSession.getId());
            //log.info("获取到的SessionID：" + httpSession.getId());
        }
        super.modifyHandshake(sec, request, response);

    }

    @Bean
    public ServerEndpointExporter serverEndpointConfigurator(){
        return new ServerEndpointExporter();
    }
}