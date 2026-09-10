package com.stratasearch.backend.validation;

import com.stratasearch.backend.exception.ApiException;
import com.stratasearch.backend.model.CorpusSession;
import com.stratasearch.backend.model.SessionStore;

import java.util.List;

public final class RequestValidator {
    private RequestValidator() {}

    public static CorpusSession RequireSession(SessionStore Store, String SessionId) {
        if (SessionId == null || SessionId.trim().isEmpty()) {
            throw new ApiException(400, "sessionId is required");
        }
        CorpusSession S = Store.Find(SessionId.trim());
        if (S == null) {
            throw new ApiException(404, "Session not found or expired: " + SessionId);
        }
        return S;
    }

    public static String[] RequirePatterns(List<String> Patterns) {
        if (Patterns == null || Patterns.isEmpty()) {
            throw new ApiException(400, "patterns must be a non-empty array");
        }
        String[] Out = new String[Patterns.size()];
        for (int I = 0; I < Patterns.size(); I++) {
            String P = Patterns.get(I);
            if (P == null || P.trim().isEmpty()) {
                throw new ApiException(400, "patterns[" + I + "] must not be blank");
            }
            Out[I] = P.trim().toLowerCase();
        }
        return Out;
    }

    // Normalize multi-word patterns the same way the console app did.
    public static String[] NormalizePatterns(List<String> Patterns) {
        String[] Raw = RequirePatterns(Patterns);
        String[] Out = new String[Raw.length];
        for (int I = 0; I < Raw.length; I++) {
            Out[I] = Raw[I];
        }
        return Out;
    }
}
