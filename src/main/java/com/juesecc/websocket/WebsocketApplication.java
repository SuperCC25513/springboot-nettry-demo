package com.juesecc.websocket;

import com.juesecc.websocket.ioNetty.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketApplication.class, args);

        try {
            System.out.println("http://127.0.0.1:8080/ws/index");
            new NettyServer(12345).start();
        }catch(Exception e) {
            System.out.println("NettyServerError:"+e.getMessage());
        }
    }

}
