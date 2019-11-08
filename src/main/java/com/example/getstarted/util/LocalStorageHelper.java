package com.example.getstarted.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

public class LocalStorageHelper implements BucketHelper {

    @Override
    public String uploadFile(Part filePart, String bucketName) throws IOException {
        return null;
    }

    @Override
    public String getObjectKey(HttpServletRequest req, HttpServletResponse resp, String bucket) throws IOException, ServletException {
        return null;
    }

    @Override
    public String searchMediaUrl(String objectKey, String bucket) {
        return null;
    }
}
