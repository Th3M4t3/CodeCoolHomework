package com.example.codecool.entity.end;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "is_processed_data")
public class ProcessedDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            nullable = false
    )
    private Long id;
    @Column(
            name = "processed_data_id",
            nullable = false
    )
    private Long processedDataId;

    public ProcessedDataEntity(Long processedDataId) {
        this.processedDataId = processedDataId;
    }

}
