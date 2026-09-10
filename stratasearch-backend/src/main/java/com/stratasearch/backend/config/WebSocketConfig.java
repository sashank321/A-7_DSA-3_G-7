package com.stratasearch.backend.config;

import com.stratasearch.backend.ws.JobProgressWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final JobProgressWebSocketHandler Handler;

    public WebSocketConfig(JobProgressWebSocketHandler Handler) {
        this.Handler = Handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry Registry2) {
        Registry2.addHandler(Handler, "/ws/jobs/*")
                .setAllowedOrigins("http://localhost:5173");
    }
}
