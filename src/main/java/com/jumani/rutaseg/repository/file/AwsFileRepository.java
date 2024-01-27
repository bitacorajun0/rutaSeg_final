package com.jumani.rutaseg.repository.file;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.jumani.rutaseg.dto.result.Error;
import com.jumani.rutaseg.dto.result.Result;
import com.jumani.rutaseg.util.DateGen;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Profile("!local & !integration_test")
// TODO revisar AmazonServiceException status code para posibles retries
public class AwsFileRepository implements FileRepository, DateGen {
    private final AmazonS3 client;
    private final String bucket;

    @Override
    public Result<Optional<String>> findLinkToFile(String key) {
        try {
            String fileLink = null;
            if (client.doesObjectExist(bucket, key)) {
                final Date expirationDate = Date.from(this.currentDateUTC().plusDays(3).toInstant());
                fileLink = client.generatePresignedUrl(bucket, key, expirationDate).toString();
            }
            return Result.response(Optional.ofNullable(fileLink));
        } catch (SdkClientException awsEx) {
            final String errorMessage = String.format("could not generate AWS link for file with key [%s]", key);
            log.error(errorMessage, awsEx);
            return Result.error("file_get_error", errorMessage);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public Optional<Error> save(String key, MultipartFile file) {
        try {
            final File fileObj = this.convertToFile(file);
            client.putObject(new PutObjectRequest(bucket, key, fileObj));
            fileObj.delete();

            return Optional.empty();
        } catch (IOException e) {
            final String errorMessage = String.format("could not convert multiFile to file for key [%s]", key);
            log.error(errorMessage, e);
            return Optional.of(new Error("file_conversion_error", errorMessage));

        } catch (SdkClientException awsEx) {
            final String errorMessage = String.format("could not save file with key [%s] to AWS", key);
            log.error(errorMessage, awsEx);
            return Optional.of(new Error("file_save_error", errorMessage));
        }
    }

    @Override
    public Optional<Error> delete(String key) {
        try {
            client.deleteObject(bucket, key);
            return Optional.empty();

        } catch (SdkClientException awsEx) {
            final String errorMessage = String.format("could not delete file with key [%s] from AWS", key);
            log.error(errorMessage, awsEx);
            return Optional.of(new Error("file_delete_error", errorMessage));
        }
    }

    private File convertToFile(MultipartFile file) throws IOException {
        final File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        }
        return convertedFile;
    }
}
