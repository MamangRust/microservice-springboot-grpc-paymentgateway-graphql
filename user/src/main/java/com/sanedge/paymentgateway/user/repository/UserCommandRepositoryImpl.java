package com.sanedge.paymentgateway.user.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.user.models.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class UserCommandRepositoryImpl implements UserCommandRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public User trashed(Long userId) {
        User user = em.find(User.class, userId);
        if (user != null) {
            em.createNativeQuery(
                    "UPDATE users SET deleted_at = CURRENT_TIMESTAMP " +
                            "WHERE user_id = :userId AND deleted_at IS NULL")
                    .setParameter("userId", userId)
                    .executeUpdate();
            em.refresh(user);
        }
        return user;
    }

    @Override
    @Transactional
    public User restore(Long userId) {
        User user = em.find(User.class, userId);
        if (user != null) {
            em.createNativeQuery(
                    "UPDATE users SET deleted_at = NULL " +
                            "WHERE user_id = :userId AND deleted_at IS NOT NULL")
                    .setParameter("userId", userId)
                    .executeUpdate();
            em.refresh(user);
        }
        return user;
    }

    @Override
    @Transactional
    public User deletePermanent(Long userId) {
        User user = em.find(User.class, userId);
        if (user != null) {
            em.createNativeQuery(
                    "DELETE FROM user_roles WHERE user_id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM users WHERE user_id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            em.detach(user);
        }
        return user;
    }

    @Override
    @Transactional
    public boolean restoreAllDeleted() {
        int updated = em.createNativeQuery(
                "UPDATE users SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deleteAllDeleted() {
        // Handle child relations first
        em.createNativeQuery(
                "DELETE FROM user_roles WHERE user_id IN (SELECT user_id FROM users WHERE deleted_at IS NOT NULL)")
                .executeUpdate();

        int deleted = em.createNativeQuery(
                "DELETE FROM users WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        return deleted > 0;
    }
}
