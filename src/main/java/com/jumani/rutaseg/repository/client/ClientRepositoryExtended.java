package com.jumani.rutaseg.repository.client;

import org.springframework.lang.Nullable;
import com.jumani.rutaseg.domain.Client;

import java.util.List;

public interface ClientRepositoryExtended {

    List<Client> search(@Nullable Long userId,
                        @Nullable String nameLike,
                        @Nullable String phoneLike,
                        @Nullable Long cuit,
                        Boolean withUser, int offset,
                        int limit);

    long count(@Nullable Long userId,
               @Nullable String nameLike,
               @Nullable String phoneLike,
               @Nullable Long cuit, Boolean withUser);
}
