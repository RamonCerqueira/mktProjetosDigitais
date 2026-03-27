package com.mktplace.service;

import com.mktplace.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class LocalS3StorageService implements StorageService {
    private final Path root;

    public LocalS3StorageService(@Value("${app.storage.local-root:storage}") String localRoot) {
        this.root = Path.of(localRoot).toAbsolutePath().normalize();
    }

    @Override
    public String upload(String key, InputStream inputStream) {
        try {
            Path target = root.resolve(key).normalize();
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            return key;
        } catch (IOException e) {
            throw new BusinessException("Falha ao armazenar arquivo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public InputStream read(String key) {
        try {
            Path target = root.resolve(key).normalize();
            if (!target.startsWith(root) || !Files.exists(target)) throw new BusinessException("Arquivo não encontrado", HttpStatus.NOT_FOUND);
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new BusinessException("Falha ao ler arquivo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
