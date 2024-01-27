package com.jumani.rutaseg.repository;

import com.jumani.rutaseg.domain.Client;
import com.jumani.rutaseg.domain.User;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@FieldNameConstants
public class UserRepositoryImpl implements UserRepositoryExtended {

    private final EntityManager entityManager;

    public List<User> search(@Nullable Boolean admin,
                             @Nullable String nicknameLike,
                             @Nullable String emailLike,
                             @Nullable Boolean withClient,
                             int offset,
                             int limit) {

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
        final Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.select(root);
        criteriaQuery.where(createPredicates(builder, criteriaQuery, root, admin, nicknameLike, emailLike, withClient));

        return entityManager.createQuery(criteriaQuery)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public long count(@Nullable Boolean admin,
                      @Nullable String nicknameLike,
                      @Nullable String emailLike,
                      @Nullable Boolean withClient) {

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        final Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.select(builder.count(root));
        criteriaQuery.where(createPredicates(builder, criteriaQuery, root, admin, nicknameLike, emailLike, withClient));

        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    private Predicate[] createPredicates(CriteriaBuilder builder,
                                         CriteriaQuery<?> query,
                                         Root<User> root,
                                         @Nullable Boolean admin,
                                         @Nullable String nicknameLike,
                                         @Nullable String emailLike,
                                         @Nullable Boolean withClient) {

        final List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(admin)) {
            predicates.add(builder.equal(root.get(User.Fields.admin), admin));
        }

        if (Objects.nonNull(nicknameLike)) {
            predicates.add(builder.like(root.get(User.Fields.nickname), "%" + nicknameLike + "%"));
        }

        if (Objects.nonNull(emailLike)) {
            predicates.add(builder.like(root.get(User.Fields.email), "%" + emailLike + "%"));
        }

        if (Objects.nonNull(withClient)) {
            final Predicate predicate;

            final Subquery<Long> subquery = query.subquery(Long.class);
            final Root<Client> clientRoot = subquery.from(Client.class);
            final Path<Long> userIdClientField = clientRoot.get(Client.Fields.user).get(User.Fields.id);
            subquery.select(userIdClientField);
            subquery.where(builder.equal(userIdClientField, root.get(User.Fields.id)));

            if (withClient) {
                predicate = builder.exists(subquery);
            } else {
                predicate = builder.not(builder.exists(subquery));
            }

            predicates.add(predicate);
        }

        return predicates.toArray(new Predicate[0]);
    }
}
