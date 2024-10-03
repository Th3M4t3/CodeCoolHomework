package com.example.codecool.service.teacher;

import com.example.codecool.dto.teacher.TeacherStatisticsDto;
import com.example.codecool.repository.end.ExamRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class StatisticsCalculatorService {
    private final ExamRepository examRepository;

    private static final Logger logger = LogManager.getLogger(StatisticsCalculatorService.class);

    StatisticsCalculatorService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    public List<TeacherStatisticsDto> statisticscalculator() {
        List<TeacherStatisticsDto> teacherStatsList = new ArrayList<>();

        // Fetch the exams where students failed (success = false)
        List<Object[]> rawResults = examRepository.getTeacherFailStatistics();
        logger.info("Retrieved {} records of teacher fail statistics.", rawResults.size());

        // Iterate over the raw results
        for (Object[] result : rawResults) {
            String teacherEmail = (String) result[0];
            Long failCount = ((Number) result[1]).longValue();

            logger.info("Processing fail statistics for teacher: {} with fail count: {}", teacherEmail, failCount);

            // Check if the TeacherStatisticsDto already exists for this teacher
            TeacherStatisticsDto teacherStatDto = teacherStatsList.stream()
                    .filter(stat -> stat.getEmail().equals(teacherEmail))
                    .findFirst()
                    .orElse(null);


            // If not found, create a new one
            if (teacherStatDto == null) {
                logger.info("Creating new statistics entry for teacher: {}", teacherEmail);
                teacherStatDto = new TeacherStatisticsDto(teacherEmail, new HashMap<>());
                teacherStatsList.add(teacherStatDto);
            }

            // Use the failCount to update the statistics
            for (long occasion = 1; occasion <= failCount; occasion++) {
                teacherStatDto.getStats().merge(occasion, 1L, Long::sum);
                logger.debug("Incremented fail count for teacher: {} on occasion: {}", teacherEmail, occasion);
            }
        }

        return teacherStatsList;

    }
}
