package com.example.codecool.repository.end;

import com.example.codecool.entity.end.ProcessedDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessedDataRepository extends JpaRepository<ProcessedDataEntity, Long> {

    @Query("SELECT p.processedDataId FROM is_processed_data p")
    List<Long> findAllProcessedDataIds();
}
