package com.stratasearch.backend.service;

import com.stratasearch.backend.dto.CorpusFingerprintResponse;
import com.stratasearch.backend.dto.UploadCorpusResponse;
import com.stratasearch.backend.engine.EngineGateway;
import com.stratasearch.backend.exception.ApiException;
import com.stratasearch.backend.mapper.FingerprintMapper;
import com.stratasearch.backend.model.CorpusSession;
import com.stratasearch.backend.model.SessionStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CorpusService {

    private final SessionStore Store;
    private final EngineGateway Gateway;

    public CorpusService(SessionStore Store, EngineGateway Gateway) {
        this.Store = Store;
        this.Gateway = Gateway;
    }

    public UploadCorpusResponse Upload(MultipartFile[] Files) {
        if (Files == null || Files.length == 0) {
            throw new ApiException(400, "At least one file is required");
        }
        if (Files.length > 100) {
            throw new ApiException(400, "Maximum documents per session is 100");
        }
        long totalBytes = 0;
        for (MultipartFile F : Files) {
            totalBytes += F.getSize();
        }
        if (totalBytes > 10 * 1024 * 1024) {
            throw new ApiException(400, "Total upload size exceeds the 10MB limit");
        }

        List<String> Docs = new ArrayList<>();
        for (MultipartFile F : Files) {
            try {
                byte[] Bytes = F.getBytes();
                String content;
                if (F.getOriginalFilename() != null && F.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                    content = extractTextFromPdf(Bytes);
                } else {
                    content = new String(Bytes, StandardCharsets.UTF_8);
                }
                Docs.add(content);
            } catch (IOException Ex) {
                throw new ApiException(400, "Failed to read file: " + F.getOriginalFilename());
            }
        }
        return RegisterSession(Docs.toArray(new String[0]));
    }

    private String extractTextFromPdf(byte[] bytes) {
        try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(bytes)) {
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception ex) {
            throw new ApiException(400, "Failed to parse PDF document: " + ex.getMessage());
        }
    }

    public UploadCorpusResponse UploadRaw(List<String> Documents) {
        if (Documents == null || Documents.isEmpty()) {
            throw new ApiException(400, "documents must be a non-empty array");
        }
        if (Documents.size() > 100) {
            throw new ApiException(400, "Maximum documents per session is 100");
        }
        long totalBytes = 0;
        for (String Doc : Documents) {
            if (Doc != null) {
                totalBytes += Doc.getBytes(StandardCharsets.UTF_8).length;
            }
        }
        if (totalBytes > 10 * 1024 * 1024) {
            throw new ApiException(400, "Total upload size exceeds the 10MB limit");
        }
        return RegisterSession(Documents.toArray(new String[0]));
    }

    private UploadCorpusResponse RegisterSession(String[] Docs) {
        String SessionId = "sess-" + UUID.randomUUID().toString().substring(0, 8);
        String Joined = Gateway.JoinDocuments(Docs);
        CorpusSession S = new CorpusSession(SessionId, Docs, Joined, System.currentTimeMillis());
        stratasearch.models.CorpusFingerprint F =
                Gateway.Profile(Docs, SessionId, false, false, 0.0);
        S.SetFingerprint(F);
        Store.Save(S);
        return new UploadCorpusResponse(SessionId, Docs.length, Joined.length());
    }

    public CorpusFingerprintResponse GetFingerprint(String SessionId) {
        CorpusSession S = Resolve(SessionId);
        return FingerprintMapper.ToDto(S.GetFingerprint());
    }

    public CorpusSession Resolve(String SessionId) {
        CorpusSession S = Store.Find(SessionId == null ? "" : SessionId.trim());
        if (S == null) {
            throw new ApiException(404, "Session not found or expired: " + SessionId);
        }
        S.Touch();
        return S;
    }
}
