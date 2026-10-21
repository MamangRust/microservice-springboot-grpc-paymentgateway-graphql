package com.sanedge.paymentgateway.role.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.role.models.Role;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class RoleCommandRepositoryImpl implements RoleCommandRepositoryCustom {
        @PersistenceContext

        private EntityManager em;

        @Override
        @Transactional
        public Role trashed(Long roleId) {
                Role role = em.find(Role.class, roleId);
                if (role != null) {
                        em.createNativeQuery(
                                        "UPDATE roles SET deleted_at = CURRENT_TIMESTAMP " +
                                                        "WHERE role_id = :roleId AND deleted_at IS NULL")
                                        .setParameter("roleId", roleId)
                                        .executeUpdate();
                        em.refresh(role);
                }
                return role;
        }

        @Override
        @Transactional
        public Role restore(Long roleId) {
                Role role = em.find(Role.class, roleId);
                if (role != null) {
                        em.createNativeQuery(
                                        "UPDATE roles SET deleted_at = NULL " +
                                                        "WHERE role_id = :roleId AND deleted_at IS NOT NULL")
                                        .setParameter("roleId", roleId)
                                        .executeUpdate();
                        em.refresh(role);
                }
                return role;
        }

        @Override
        @Transactional
        public Role deletePermanent(Long roleId) {
                Role role = em.find(Role.class, roleId);
                if (role != null) {
                        em.createNativeQuery(
                                        "DELETE FROM roles " +
                                                        "WHERE role_id = :roleId")
                                        .setParameter("roleId", roleId)
                                        .executeUpdate();
                        em.detach(role);
                }
                return role;
        }

        @Override
        @Transactional
        public boolean restoreAllDeleted() {
                int updated = em.createNativeQuery(
                                "UPDATE roles SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                                .executeUpdate();
                return updated > 0;
        }

        @Override
        @Transactional
        public boolean deleteAllDeleted() {
                int deleted = em.createNativeQuery(
                                "DELETE FROM roles WHERE deleted_at IS NOT NULL")
                                .executeUpdate();
                return deleted > 0;
        }

}
