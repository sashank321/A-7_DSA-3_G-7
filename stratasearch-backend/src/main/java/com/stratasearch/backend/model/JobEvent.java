package com.stratasearch.backend.model;

// One streaming event in a Job's lifecycle (also broadcast via WebSocket).
public class JobEvent {
    public String type;       // e.g. PROFILE_PROGRESS, PLANNER_COMPLETE, BENCHMARK_PROGRESS, SEARCH_COMPLETE, DONE, FAILED
    public long timestamp;
    public Integer progress;  // 0-100 for _PROGRESS events
    public String message;
    public Object payload;    // event-specific extras (e.g. planner summary)

    public JobEvent(String Type, Integer Progress, String Message, Object Payload) {
        this.type = Type;
        this.timestamp = System.currentTimeMillis();
        this.progress = Progress;
        this.message = Message;
        this.payload = Payload;
    }
}
