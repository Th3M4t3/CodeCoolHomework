package com.example.codecool.repository.end;

import com.example.codecool.entity.end.ExamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<ExamEntity, Long> {
    @Query("SELECT c FROM exam c WHERE c.cancelled = false AND c.date = " +
            "(SELECT MAX(c2.date) FROM exam c2 WHERE c2.className = c.className " +
            "AND c2.student = c.student AND c2.cancelled = false)")
    List<ExamEntity> findLatestNonCanceledClassesForAllStudents();

    @Query("SELECT c FROM exam c WHERE c.cancelled = false AND c.student = :student " +
            "AND c.date = (SELECT MAX(c2.date) FROM exam c2 " +
            "WHERE c2.className = c.className AND c2.student = :student AND c2.cancelled = false)")
    List<ExamEntity> findLatestNonCanceledExamsForStudent(@Param("student") String student);

    @Query("SELECT c.teacher, COUNT(c.id) " +
            "FROM exam c " +
            "WHERE c.success = false " +
            "GROUP BY c.teacher, c.className, c.student")
    List<Object[]> getTeacherFailStatistics();
}
