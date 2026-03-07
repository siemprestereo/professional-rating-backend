package com.example.waiter_rating.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.waiter_rating.service.CloudinaryService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public Map<String, Object> generateSignedUploadParams(Long userId) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000L;
        String publicId = "profile-photos/" + userId + "/avatar";

        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        params.put("public_id", publicId);
        params.put("overwrite", true);

        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        Map<String, Object> result = new HashMap<>(params);
        result.put("signature", signature);
        result.put("api_key", cloudinary.config.apiKey);
        result.put("cloud_name", cloudinary.config.cloudName);
        result.put("upload_url", "https://api.cloudinary.com/v1_1/"
                + cloudinary.config.cloudName + "/image/upload");

        return result;
    }

    @Override
    public String verifyAndBuildUrl(Long userId, String publicId) throws Exception {
        String expectedPrefix = "profile-photos/" + userId + "/";
        if (!publicId.startsWith(expectedPrefix)) {
            throw new SecurityException("El public_id no pertenece al usuario autenticado");
        }

        // Verifica que existe Y obtiene el version actual
        Map resource = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
        Long version = ((Number) resource.get("version")).longValue();

        // Incluir version en la URL para invalidar caché
        return cloudinary.url()
                .secure(true)
                .version(version)
                .transformation(new Transformation()
                        .width(400).height(400)
                        .crop("fill").gravity("face")
                        .quality("auto").fetchFormat("auto"))
                .generate(publicId);
    }
}