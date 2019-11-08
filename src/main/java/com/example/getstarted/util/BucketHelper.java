package com.example.getstarted.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

public interface BucketHelper {

    String uploadFile(Part filePart, final String bucketName) throws IOException;

    String getObjectKey(HttpServletRequest req, HttpServletResponse resp, final String bucket) throws IOException, ServletException;

    String searchMediaUrl(String objectKey, final String bucket);
}
