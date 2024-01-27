package com.jumani.rutaseg.repository;

import com.jumani.rutaseg.domain.User;
import org.springframework.lang.Nullable;

import java.util.List;

public interface UserRepositoryExtended {
    List<User> search(
            @Nullable Boolean admin,
            @Nullable String nicknameLike,
            @Nullable String emailLike,
            @Nullable Boolean withClient,
            int offset,
            int limit);

    long count(@Nullable Boolean admin,
               @Nullable String nicknameLike,
               @Nullable String emailLike, Boolean withClient);
}
