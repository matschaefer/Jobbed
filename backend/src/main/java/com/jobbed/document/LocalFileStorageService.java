package com.jobbed.document;

import com.jobbed.common.error.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path basePath;

    public LocalFileStorageService(@Value("${app.file-storage.local-path:${FILE_STORAGE_LOCAL_PATH:./uploads}}") String path) {
        this.basePath = Path.of(path).toAbsolutePath().normalize();
        try { Files.createDirectories(basePath); }
        catch (IOException ex) { throw new IllegalStateException("Dateispeicher konnte nicht initialisiert werden.", ex); }
    }

    @Override
    public StoredFile store(byte[] content, String extension) {
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        String storedName = UUID.randomUUID() + "." + extension;
        Path directory = basePath.resolve(String.valueOf(now.getYear())).resolve(String.format("%02d", now.getMonthValue()));
        Path target = safe(directory.resolve(storedName));
        try {
            Files.createDirectories(directory);
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
            return new StoredFile(storedName, basePath.relativize(target).toString().replace('\\', '/'));
        } catch (IOException ex) { throw new IllegalStateException("Datei konnte nicht gespeichert werden.", ex); }
    }

    @Override public InputStream open(String relativePath) {
        Path path = safe(basePath.resolve(relativePath));
        try { return Files.newInputStream(path, StandardOpenOption.READ); }
        catch (NoSuchFileException ex) { throw new ResourceNotFoundException("Die gespeicherte Datei wurde nicht gefunden."); }
        catch (IOException ex) { throw new IllegalStateException("Datei konnte nicht geöffnet werden.", ex); }
    }

    @Override public void delete(String relativePath) {
        try { Files.deleteIfExists(safe(basePath.resolve(relativePath))); }
        catch (IOException ex) { throw new IllegalStateException("Datei konnte nicht gelöscht werden.", ex); }
    }

    private Path safe(Path candidate) {
        Path normalized = candidate.toAbsolutePath().normalize();
        if (!normalized.startsWith(basePath)) throw new SecurityException("Ungültiger Speicherpfad.");
        return normalized;
    }
}
