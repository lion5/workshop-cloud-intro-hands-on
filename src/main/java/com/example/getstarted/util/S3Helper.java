package com.example.getstarted.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class S3Helper implements BucketHelper {
    private static final Logger logger = Logger.getLogger(S3Helper.class.getName());

    private static final String AWS_REGION = System.getenv("AWS_REGION");

    private final AmazonS3 client;

    public S3Helper() {
        client = AmazonS3ClientBuilder.standard()
                                      .withRegion(AWS_REGION)
                                      .withCredentials(new DefaultAWSCredentialsProviderChain())
                                      .build();
    }

    @Override
    public String uploadFile(Part filePart, final String bucketName) throws IOException {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("YYYY-MM-dd-HHmmssSSS-");
        DateTime dt = DateTime.now(DateTimeZone.UTC);
        String dtString = dt.toString(dtf);
        final String processedFileName = dtString + filePart.getSubmittedFileName();
        final String originalFileName = "rawimg-" + processedFileName;

        // upload file to S3 bucket
        InputStream fileContent = filePart.getInputStream();
        File tempFile = File.createTempFile("prefix", "suffix");
        try {
            FileUtils.copyInputStreamToFile(fileContent, tempFile);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, originalFileName, tempFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            logger.log(Level.INFO, "Uploading Image file to S3", putObjectRequest);
            client.putObject(putObjectRequest);

            return processedFileName;
        } catch (AmazonS3Exception s3e) {
            logger.log(Level.SEVERE, s3e.getMessage());
            return "";
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Override
    public String getObjectKey(HttpServletRequest req, HttpServletResponse resp, final String bucket) throws IOException, ServletException {
        Part filePart = req.getPart("file");
        final String fileName = filePart.getSubmittedFileName();
        String objectKey = req.getParameter("objectKey");
        // Check extension of file
        if (fileName != null && !fileName.isEmpty() && fileName.contains(".")) {
            final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            String normalizedExtension = extension.toLowerCase();
            // Amazon Rekognition supports JPG and PNG files
            String[] allowedExt = {"jpg", "jpeg", "png"};
            for (String s : allowedExt) {
                if (normalizedExtension.equals(s)) {
                    return this.uploadFile(filePart, bucket);
                }
            }
            throw new ServletException("file must be an image");
        }
        return objectKey;
    }

    @Override
    public String searchMediaUrl(String objectKey, final String bucket) {
        String result = "";
        if ("".equals(objectKey)) {
            return "";
        } else if (client.doesObjectExist(bucket, objectKey)) {
            return client.getUrl(bucket, objectKey).toString();
        }
        return result;
    }
}

