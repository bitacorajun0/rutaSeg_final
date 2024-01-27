package com.jumani.rutaseg.repository.file;

import com.jumani.rutaseg.dto.result.Error;
import com.jumani.rutaseg.dto.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FileRepository {
    Result<Optional<String>> findLinkToFile(String key);

    Optional<Error> save(String key, MultipartFile file);

    Optional<Error> delete(String key);
}
