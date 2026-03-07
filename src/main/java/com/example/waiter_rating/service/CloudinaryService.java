package com.example.waiter_rating.service;

import java.util.Map;

public interface CloudinaryService {

    Map<String, Object> generateSignedUploadParams(Long userId) throws Exception;

    String verifyAndBuildUrl(Long userId, String publicId) throws Exception;
}