package com.menstalk.authservice.repositories;

import com.menstalk.authservice.models.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {
    @Query("select a from Auth a where a.username = :username")
    Auth findByUsername(@Param("username") String username);
}
