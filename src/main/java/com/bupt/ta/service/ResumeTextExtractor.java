package com.bupt.ta.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 从常见简历文件中抽取纯文本（.txt / .pdf / .doc / .docx）。
 */
public final class ResumeTextExtractor {

    private ResumeTextExtractor() {}

    public static boolean isSupportedFilename(String filename) {
        if (filename == null) return false;
        String l = filename.toLowerCase();
        return l.endsWith(".txt") || l.endsWith(".pdf") || l.endsWith(".doc") || l.endsWith(".docx");
    }

    /**
     * @param data     文件完整字节
     * @param filename 用于判断扩展名（可含路径）
     */
    public static String extractFromBytes(byte[] data, String filename) throws IOException {
        if (data == null || data.length == 0) {
            return "";
        }
        String lower = filename != null ? filename.toLowerCase() : "";
        if (lower.endsWith(".txt")) {
            return new String(data, StandardCharsets.UTF_8);
        }
        if (lower.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(data)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(doc);
            }
        }
        if (lower.endsWith(".docx")) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(data);
                 XWPFDocument doc = new XWPFDocument(in)) {
                try (XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
                    return ex.getText();
                }
            }
        }
        if (lower.endsWith(".doc")) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(data);
                 HWPFDocument doc = new HWPFDocument(in)) {
                try (WordExtractor ex = new WordExtractor(doc)) {
                    return ex.getText();
                }
            }
        }
        throw new IllegalArgumentException("unsupported resume format");
    }
}
