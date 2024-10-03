package com.example.codecool.repository.end;

import com.example.codecool.entity.end.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByEmail(String email);
    List<UserEntity> findAllByIsStudent(Boolean isStudent);
}
