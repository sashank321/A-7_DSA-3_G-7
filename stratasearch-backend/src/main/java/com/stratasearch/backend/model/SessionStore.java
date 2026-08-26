package com.stratasearch.backend.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// In-memory store of corpus sessions. Stateless engine, metadata-only sessions.
@Component
public class SessionStore {

    private final Map<String, CorpusSession> Sessions = new ConcurrentHashMap<>();
    private final long TtlMillis;

    public SessionStore(@Value("${stratasearch.session.ttl-minutes:30}") long TtlMinutes) {
        this.TtlMillis = TtlMinutes * 60_000L;
    }

    public CorpusSession Save(CorpusSession Session) {
        Sessions.put(Session.GetSessionId(), Session);
        return Session;
    }

    public CorpusSession Find(String SessionId) {
        return Sessions.get(SessionId);
    }

    public boolean Exists(String SessionId) {
        return Sessions.containsKey(SessionId);
    }

    public void Touch(String SessionId) {
        CorpusSession S = Sessions.get(SessionId);
        if (S != null) {
            S.Touch();
        }
    }

    @Scheduled(fixedDelayString = "${stratasearch.session.sweep-interval-ms:60000}")
    public void SweepExpired() {
        long Now = System.currentTimeMillis();
        List<String> ToRemove = new ArrayList<>();
        for (Map.Entry<String, CorpusSession> E : Sessions.entrySet()) {
            if (Now - E.getValue().GetLastTouchedEpochMs() > TtlMillis) {
                ToRemove.add(E.getKey());
            }
        }
        for (String Id : ToRemove) {
            Sessions.remove(Id);
        }
    }

    public int ActiveCount() {
        return Sessions.size();
    }
}
