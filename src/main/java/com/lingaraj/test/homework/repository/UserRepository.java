package com.lingaraj.test.homework.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lingaraj.test.homework.entity.User;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findFirstByUserId(String userId);

    Optional<User> findFirstByUserIdAndPassword(String userId, String password);
}
