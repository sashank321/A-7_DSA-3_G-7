import type { JobEvent, WsClosedFrame, WsFrame } from "./types";

export interface JobSocketHandlers {
  /** Full authoritative snapshot pushed every ~200ms by the server. */
  onSnapshot?: (events: JobEvent[]) => void;
  /** Terminal frame: job finished. Client closes the socket after this. */
  onClosed?: (status: "SUCCEEDED" | "FAILED", result: unknown, error: string | null) => void;
  onError?: (ev: Event) => void;
}

/**
 * Subscribe to live job progress over the backend WebSocket.
 *
 * Server contract (JobProgressWebSocketHandler):
 *   1. Screen: ws://{host}/ws/jobs/{jobId}
 *   2. First frame: { "type": "SUBSCRIBED", "jobId": ... }
 *   3. Then full JobEvent[] snapshots every 200 ms (parsable via Array.isArray).
 *   4. Terminal frame: { "type": "CLOSED", "status", "result", "error" } — server
 *      does NOT close the socket; the client must close() it.
 *
 * In dev this goes through the Vite proxy (/ws → ws://localhost:8080).
 */
export function subscribeToJob(jobId: string, handlers: JobSocketHandlers): () => void {
  const proto = window.location.protocol === "https:" ? "wss" : "ws";
  const socket = new WebSocket(`${proto}://${window.location.host}/ws/jobs/${jobId}`);
  let done = false;

  socket.onmessage = (msg) => {
    try {
      const frame = JSON.parse(msg.data as string) as WsFrame;
      if (Array.isArray(frame)) {
        handlers.onSnapshot?.(frame);
        return;
      }
      if (frame.type === "CLOSED") {
        done = true;
        const closed = frame as WsClosedFrame;
        handlers.onClosed?.(closed.status, closed.result, closed.error);
        socket.close();
        return;
      }
      // SUBSCRIBED frame — no handler needed, server follows with snapshots
    } catch {
      /* malformed frame — ignore */
    }
  };

  socket.onerror = (ev) => {
    if (!done) handlers.onError?.(ev);
  };

  return () => {
    if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
      socket.close();
    }
  };
}
