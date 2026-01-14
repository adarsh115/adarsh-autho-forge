package com.adarsh.autho.forge.service.repository.user;

import com.adarsh.autho.forge.service.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AuthUser, Long>, UserRepositoryCustom {
    boolean existsByUsername(String username);
    Optional<AuthUser> findByUsername(String username);
}
