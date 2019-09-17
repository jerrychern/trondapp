package shop.tronlucky.trondapp.controller.websocket;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import shop.tronlucky.trondapp.model.RoundInfo;
import shop.tronlucky.trondapp.service.RoundInfoService;
import shop.tronlucky.trondapp.util.JSONUtil;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/websocket/purchaseList")
@Component
@CrossOrigin
public class RoundInfoSocketServer {
    private static CopyOnWriteArraySet<RoundInfoSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    private Session session;

    private static RoundInfoService roundInfoService;

    @Autowired
    public RoundInfoSocketServer(RoundInfoService roundInfoService) {
        RoundInfoSocketServer.roundInfoService = roundInfoService;
    }

    public RoundInfoSocketServer() {

    }

    @OnOpen
    public void onOpen(Session session) {
        RoundInfo roundInfo = roundInfoService.findRoundInfoByRound(null);
        this.session = session;
        webSocketSet.add(this);
        sendMessage(JSONObject.toJSONString(roundInfo));
    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
    }

    @OnMessage
    public void onMessage(String message) {

    }

    @OnError
    public void onError(Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message){
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendInfo(String message){
        for (RoundInfoSocketServer item : webSocketSet) {
            item.sendMessage(message);
        }
    }
}