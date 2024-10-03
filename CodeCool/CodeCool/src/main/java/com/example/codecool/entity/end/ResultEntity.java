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
@Entity(name = "result")
public class ResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id",
            nullable = false
    )
    private ExamEntity examId;

    @Column(name = "dimension",
            nullable = false
    )
    private String dimension;

    @Column(name = "result",
            nullable = false
    )
    private Integer result;

    public ResultEntity(ExamEntity examId,
                        String dimension,
                        Integer result) {
        this.examId = examId;
        this.dimension = dimension;
        this.result = result;
    }
}
