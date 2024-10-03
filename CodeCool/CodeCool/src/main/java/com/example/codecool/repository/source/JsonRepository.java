package com.example.codecool.repository.source;

import com.example.codecool.entity.source.JsonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsonRepository extends JpaRepository<JsonEntity, Long> {

    List<JsonEntity> findAllByIdNotIn(List<Long> ids);
}
