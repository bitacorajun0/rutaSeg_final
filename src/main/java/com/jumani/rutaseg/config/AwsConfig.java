package com.jumani.rutaseg.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.jumani.rutaseg.repository.file.AwsFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local & !integration_test")
public class AwsConfig {
    
    @Value("${aws.credentials.access-key}")
    private String accessKey;
    @Value("${aws.credentials.secret-key}")
    private String accessSecret;
    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.files-bucket}")
    private String filesBucket;
    

    @Bean
    public AmazonS3 s3Client() {
        final AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecret);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
    }
    
    @Bean
    public AwsFileRepository awsFileRepository(AmazonS3 client) {
        return new AwsFileRepository(client, filesBucket);
    }
}
