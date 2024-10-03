package com.example.codecool.entity.end;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "exam")
public class ExamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class",
            nullable = false)
    @JsonProperty("class")
    private String className;

    @Column(name = "teacher",
            nullable = false)
    private String teacher;

    @Column(name = "student",
            nullable = false
    )
    private String student;

    @Column(name = "date",
            nullable = false
    )
    private LocalDate date;

    @Column(name = "cancelled",
            nullable = false
    )
    private Boolean cancelled;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "comment",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String comment;

    @OneToMany(mappedBy = "examId",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ResultEntity> results;

    public ExamEntity(String className,
                      String teacher,
                      String student,
                      LocalDate date,
                      Boolean cancelled,
                      Boolean success,
                      String comment,
                      List<ResultEntity> results) {
        this.className = className;
        this.teacher = teacher;
        this.student = student;
        this.date = date;
        this.cancelled = cancelled;
        this.success = success;
        this.comment = comment;
        this.results = results;
    }
    @Override
    public String toString() {
        return "ExamEntity{" +
                "className='" + className + '\'' +
                ", teacher='" + teacher + '\'' +
                ", student='" + student + '\'' +
                ", date=" + date +
                ", cancelled=" + cancelled +
                ", comment='" + comment + '\'' +
                ", results=" + results +
                ", success=" + success +
                '}';
    }
}
