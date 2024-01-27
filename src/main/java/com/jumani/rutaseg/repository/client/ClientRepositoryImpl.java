package com.jumani.rutaseg.repository.client;

import com.jumani.rutaseg.domain.Client;
import com.jumani.rutaseg.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class ClientRepositoryImpl implements ClientRepositoryExtended {

    private final EntityManager entityManager;

    public ClientRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Client> search(@Nullable Long userId,
                               @Nullable String nameLike,
                               @Nullable String phoneLike,
                               @Nullable Long cuit,
                               @Nullable Boolean withUser,
                               int offset,
                               int limit) {

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Client> criteriaQuery = builder.createQuery(Client.class);
        final Root<Client> root = criteriaQuery.from(Client.class);

        criteriaQuery.select(root);
        criteriaQuery.where(this.createPredicates(builder, root, userId, nameLike, phoneLike, cuit, withUser));
        criteriaQuery.orderBy(builder.asc(root.get(Client.Fields.id)));

        return entityManager.createQuery(criteriaQuery)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public long count(@Nullable Long userId,
                      @Nullable String nameLike,
                      @Nullable String phoneLike,
                      @Nullable Long cuit,
                      @Nullable Boolean withUser) {

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        final Root<Client> root = countQuery.from(Client.class);

        countQuery.select(builder.countDistinct(root));
        countQuery.where(this.createPredicates(builder, root, userId, nameLike, phoneLike, cuit, withUser));

        return entityManager.createQuery(countQuery).getSingleResult();

    }

    private Predicate[] createPredicates(CriteriaBuilder builder, Root<Client> root,
                                         @Nullable Long userId,
                                         @Nullable String nameLike,
                                         @Nullable String phoneLike,
                                         @Nullable Long cuit,
                                         @Nullable Boolean withUser) {

        final List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(userId)) {
            predicates.add(builder.equal(root.get(Client.Fields.user).get(User.Fields.id), userId));
        } else if (Objects.nonNull(withUser)) {
            if (withUser) {
                predicates.add(builder.isNotNull(root.get(Client.Fields.user)));
            } else {
                predicates.add(builder.isNull(root.get(Client.Fields.user)));
            }
        }

        if (Objects.nonNull(nameLike)) {
            predicates.add(builder.like(builder.lower(root.get(Client.Fields.name)), "%" + nameLike.toLowerCase() + "%"));
        }

        if (Objects.nonNull(phoneLike)) {
            predicates.add(builder.like(builder.lower(root.get(Client.Fields.phone)), "%" + phoneLike.toLowerCase() + "%"));
        }

        if (Objects.nonNull(cuit)) {
            predicates.add(builder.equal(root.get(Client.Fields.cuit), cuit));
        }

        return predicates.toArray(new Predicate[0]);
    }
}
