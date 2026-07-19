package com.jobbed.document;

import com.jobbed.common.error.exception.FileValidationException;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.zip.*;
import static org.assertj.core.api.Assertions.*;

class FileContentValidatorTest {
    private final FileContentValidator validator = new FileContentValidator();

    @Test void detectsPdfFromMagicBytes() {
        var result = validator.validate("%PDF-1.7\ncontent".getBytes(), "application/pdf");
        assertThat(result.extension()).isEqualTo("pdf");
    }
    @Test void rejectsSpoofedMimeType() {
        assertThatThrownBy(() -> validator.validate("%PDF-1.7".getBytes(), "image/png"))
                .isInstanceOf(FileValidationException.class);
    }
    @Test void acceptsOnlyRealDocxZipStructure() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml")); zip.write("types".getBytes()); zip.closeEntry();
            zip.putNextEntry(new ZipEntry("word/document.xml")); zip.write("document".getBytes()); zip.closeEntry();
        }
        assertThat(validator.validate(out.toByteArray(),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document").extension()).isEqualTo("docx");
    }
}
