package com.mktplace.service;

import java.io.InputStream;

public interface StorageService {
    String upload(String key, InputStream inputStream);
    InputStream read(String key);
}
