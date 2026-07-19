package com.jobbed.document;

import com.jobbed.common.error.ErrorCode;
import com.jobbed.common.error.exception.FileValidationException;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class FileContentValidator {
    public DetectedFile validate(byte[] bytes, String declaredMimeType) {
        if (bytes.length == 0) throw unsupported("Die Datei ist leer.");
        DetectedFile detected = detect(bytes);
        if (detected == null) throw unsupported("Dateityp oder Dateiinhalt wird nicht unterstützt.");
        if (declaredMimeType != null && !declaredMimeType.isBlank()
                && !"application/octet-stream".equalsIgnoreCase(declaredMimeType)
                && !detected.mimeType().equalsIgnoreCase(declaredMimeType)) {
            throw unsupported("Der angegebene Dateityp stimmt nicht mit dem Dateiinhalt überein.");
        }
        return detected;
    }

    private DetectedFile detect(byte[] b) {
        if (starts(b, new byte[]{0x25,0x50,0x44,0x46,0x2d})) return new DetectedFile("application/pdf", "pdf");
        if (starts(b, new byte[]{(byte)0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a})) return new DetectedFile("image/png", "png");
        if (starts(b, new byte[]{(byte)0xff,(byte)0xd8,(byte)0xff})) return new DetectedFile("image/jpeg", "jpg");
        if (starts(b, new byte[]{(byte)0xd0,(byte)0xcf,0x11,(byte)0xe0,(byte)0xa1,(byte)0xb1,0x1a,(byte)0xe1}))
            return new DetectedFile("application/msword", "doc");
        if (starts(b, new byte[]{0x50,0x4b,0x03,0x04}) && isDocx(b))
            return new DetectedFile("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        return null;
    }

    private boolean isDocx(byte[] bytes) {
        boolean contentTypes = false, word = false; int entries = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null && entries++ < 200) {
                String name = entry.getName();
                if ("[Content_Types].xml".equals(name)) contentTypes = true;
                if (name.startsWith("word/")) word = true;
                if (contentTypes && word) return true;
            }
        } catch (IOException ignored) { return false; }
        return false;
    }

    private boolean starts(byte[] bytes, byte[] signature) {
        return bytes.length >= signature.length && Arrays.equals(Arrays.copyOf(bytes, signature.length), signature);
    }
    private FileValidationException unsupported(String message) {
        return new FileValidationException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, message);
    }
    public record DetectedFile(String mimeType, String extension) {}
}
