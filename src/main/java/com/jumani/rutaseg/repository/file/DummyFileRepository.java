package com.jumani.rutaseg.repository.file;

import com.jumani.rutaseg.dto.result.Error;
import com.jumani.rutaseg.dto.result.Result;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Component
@Profile("local | integration_test")
public class DummyFileRepository implements FileRepository {
    private final List<String> links = new ArrayList<>();

    @Override
    public Result<Optional<String>> findLinkToFile(String key) {
        String linkToFile = null;
        if (links.contains(key)) {
            linkToFile = key;
        }
        return Result.response(Optional.ofNullable(linkToFile));
    }

    @Override
    public Optional<Error> save(String key, MultipartFile file) {
        links.add(key);
        return Optional.empty();
    }

    @Override
    public Optional<Error> delete(String key) {
        links.remove(key);
        return Optional.empty();
    }
}
