package com.stratasearch.backend.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratasearch.backend.model.Job;
import com.stratasearch.backend.model.JobEvent;
import com.stratasearch.backend.model.JobRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Plain WebSocket handler that streams JobEvent JSON to clients subscribed to /ws/jobs/{jobId}.
@org.springframework.stereotype.Component
public class JobProgressWebSocketHandler extends TextWebSocketHandler {

    private final JobRegistry Registry;
    private final ObjectMapper Mapper = new ObjectMapper();
    private final Map<String, List<WebSocketSession>> Subscribers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService Streamer = Executors.newScheduledThreadPool(1);

    public JobProgressWebSocketHandler(JobRegistry Registry) {
        this.Registry = Registry;
        Streamer.scheduleAtFixedRate(this::Fanout, 200, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession Session) throws Exception {
        String Path = Session.getUri() == null ? "" : Session.getUri().getPath();
        String JobId = Path.substring(Path.lastIndexOf('/') + 1);
        Subscribers.computeIfAbsent(JobId, K -> new CopyOnWriteArrayList<>()).add(Session);
        Session.sendMessage(new TextMessage("{\"type\":\"SUBSCRIBED\",\"jobId\":\"" + JobId + "\"}"));

        Job J = Registry.Find(JobId);
        if (J != null) {
            SendJson(Session, J.GetEvents());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession Session, CloseStatus Status) {
        for (List<WebSocketSession> L : Subscribers.values()) {
            L.removeIf(S -> S.getId().equals(Session.getId()));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession Session, Throwable Ex) throws Exception {
        Session.close(CloseStatus.SERVER_ERROR);
    }

    private void Fanout() {
        for (Map.Entry<String, List<WebSocketSession>> E : Subscribers.entrySet()) {
            String JobId = E.getKey();
            Job J = Registry.Find(JobId);
            if (J == null) continue;
            List<WebSocketSession> Subs = E.getValue();
            for (WebSocketSession S : Subs) {
                if (!S.isOpen()) continue;
                try {
                    SendJson(S, J.GetEvents());
                    if (J.GetStatus() == Job.Status.SUCCEEDED || J.GetStatus() == Job.Status.FAILED) {
                        Map<String, Object> Done = new java.util.HashMap<>();
                        Done.put("type", "CLOSED");
                        Done.put("status", J.GetStatus().name());
                        Done.put("result", J.GetResult());
                        Done.put("error", J.GetErrorMessage());
                        S.sendMessage(new TextMessage(Mapper.writeValueAsString(Done)));
                    }
                } catch (IOException Ex) {
                    // swallow; cleanup happens on next heartbeat or connection close
                }
            }
        }
    }

    private void SendJson(WebSocketSession S, List<JobEvent> Events) throws IOException {
        S.sendMessage(new TextMessage(Mapper.writeValueAsString(Events)));
    }
}
