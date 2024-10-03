package com.example.codecool.service.student;

import com.example.codecool.dto.student.Average;
import com.example.codecool.dto.student.StudentAverageDto;
import com.example.codecool.entity.end.ExamEntity;
import com.example.codecool.entity.end.ResultEntity;
import com.example.codecool.entity.end.UserEntity;
import com.example.codecool.exception.StudentNotFoundException;
import com.example.codecool.repository.end.ExamRepository;
import com.example.codecool.repository.end.UserRepository;
import com.example.codecool.service.trigger.TriggerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AverageCalculatorForStudentService {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;

    private static final Logger logger = LogManager.getLogger(AverageCalculatorForStudentService.class);


    public AverageCalculatorForStudentService(UserRepository userRepository,
                                    ExamRepository examRepository) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
    }

    public StudentAverageDto calculateAverageForStudent(Long id) {
        StudentAverageDto response = new StudentAverageDto();

        UserEntity student = userRepository.findById(id).orElse(null);
        if (student != null && student.getIsStudent()) {
            logger.info("Found student: {} with email: {}", student.getId(), student.getEmail());

            List<ExamEntity> exams = examRepository.findLatestNonCanceledExamsForStudent(student.getEmail());
            response.setAverageList(new ArrayList<>());
            response.setEmail(student.getEmail());
            Map<String, List<Integer>> dimensionScoresMap = new HashMap<>();

            logger.info("Retrieved {} exams for student: {}", exams.size(), student.getEmail());

            for (ExamEntity exam : exams) {
                if (exam.getStudent().equals(student.getEmail())) {
                    logger.debug("Processing exam ID: {} for student: {}", exam.getId(), student.getEmail());
                    for (ResultEntity result : exam.getResults()) {
                        dimensionScoresMap.putIfAbsent(result.getDimension(), new ArrayList<>());
                        dimensionScoresMap.get(result.getDimension()).add(result.getResult());
                        logger.debug("Added score: {} for dimension: {} in exam ID: {}", result.getResult(), result.getDimension(), exam.getId());
                    }
                }
            }

            for (Map.Entry<String, List<Integer>> entry : dimensionScoresMap.entrySet()) {
                String dimension = entry.getKey();
                List<Integer> scores = entry.getValue();
                double average = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                response.getAverageList().add(new Average(dimension, average));
                logger.info("Calculated average for dimension: {} is: {}", dimension, average);
            }
        } else {
            logger.error("Student not found with ID: {}", id);
            throw new StudentNotFoundException("Student not found with id: " + id);
        }

        return response;
    }
}
