package com.sanedge.paymentgateway.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.role.models.Role;

@Repository
public interface RoleCommandRepository extends JpaRepository<Role, Long>, RoleCommandRepositoryCustom {
}
