package com.jobbed.document;

import java.io.InputStream;

public interface FileStorageService {
    StoredFile store(byte[] content, String extension);
    InputStream open(String relativePath);
    void delete(String relativePath);
}
