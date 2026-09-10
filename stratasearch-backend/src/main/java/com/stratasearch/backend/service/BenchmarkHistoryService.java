package com.stratasearch.backend.service;

import com.stratasearch.backend.dto.BenchmarkResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Per-session in-memory ring buffer of past benchmark runs (metadata only).
@Service
public class BenchmarkHistoryService {

    private static final int Capacity = 100;

    public static class BenchmarkRun {
        public final long RanAtEpochMs;
        public final List<BenchmarkResponse> Rows;

        public BenchmarkRun(List<BenchmarkResponse> Rows) {
            this.RanAtEpochMs = System.currentTimeMillis();
            this.Rows = Rows;
        }
    }

    private final Map<String, Deque<BenchmarkRun>> History = new ConcurrentHashMap<>();

    public void Record(String SessionId, List<BenchmarkResponse> Rows) {
        Deque<BenchmarkRun> Q = History.computeIfAbsent(SessionId, K -> new ArrayDeque<>());
        synchronized (Q) {
            Q.addLast(new BenchmarkRun(Rows));
            while (Q.size() > Capacity) {
                Q.removeFirst();
            }
        }
    }

    public List<BenchmarkRun> ListRuns(String SessionId) {
        Deque<BenchmarkRun> Q = History.get(SessionId);
        if (Q == null) return new ArrayList<>();
        synchronized (Q) {
            return new ArrayList<>(Q);
        }
    }

    public BenchmarkRun Latest(String SessionId) {
        Deque<BenchmarkRun> Q = History.get(SessionId);
        if (Q == null) return null;
        synchronized (Q) {
            return Q.peekLast();
        }
    }
}
