package rs.ftn.newnow.storage;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObjectStorageService {

    private final MinioClient minioClient;

    @Value("${newnow.minio.bucket}")
    private String bucket;

    public void putObject(String key, InputStream data, long size, String contentType) throws IOException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(data, size, -1)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build()
            );
            log.debug("Put object key={} size={} contentType={}", key, size, contentType);
        } catch (Exception e) {
            throw new IOException("Failed to put object " + key, e);
        }
    }

    public InputStream getObject(String key) throws IOException {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw new IOException("Failed to get object " + key, e);
        }
    }

    public boolean exists(String key) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(key).build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            log.warn("statObject error for {}: {}", key, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("statObject error for {}: {}", key, e.getMessage());
            return false;
        }
    }

    public void remove(String key) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            log.warn("Failed to remove object {}: {}", key, e.getMessage());
        }
    }

    public String presignedGetUrl(String key, int expirySeconds) throws IOException {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(key)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Failed to create presigned URL for " + key, e);
        }
    }

    public String getBucket() {
        return bucket;
    }
}
