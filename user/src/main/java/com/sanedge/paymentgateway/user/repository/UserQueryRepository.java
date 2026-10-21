package com.sanedge.paymentgateway.user.repository;

import java.util.Optional;

import com.sanedge.paymentgateway.user.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQueryRepository extends JpaRepository<User, Long> {
  @Query("""
          SELECT u
          FROM User u
          WHERE u.deletedAt IS NULL
            AND (:keyword IS NULL
                 OR LOWER(u.firstname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.lastname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
          ORDER BY u.createdAt DESC
      """)
  Page<User> findAllUsers(@Param("keyword") String keyword, Pageable pageable);

  @Query("""
          SELECT u
          FROM User u
          WHERE u.deletedAt IS NULL
            AND (:keyword IS NULL
                 OR LOWER(u.firstname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.lastname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
          ORDER BY u.createdAt DESC
      """)
  Page<User> findActiveUsers(@Param("keyword") String keyword, Pageable pageable);

  @Query("""
          SELECT u
          FROM User u
          WHERE u.deletedAt IS NOT NULL
            AND (:keyword IS NULL
                 OR LOWER(u.firstname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.lastname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
          ORDER BY u.createdAt DESC
      """)
  Page<User> findTrashedUsers(@Param("keyword") String keyword, Pageable pageable);

  Optional<User> findByUserId(Long userId);

  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

}
