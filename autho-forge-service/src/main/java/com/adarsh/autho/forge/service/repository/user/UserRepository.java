package com.adarsh.autho.forge.service.repository.user;

import com.adarsh.autho.forge.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
}
