package com.example.codecool.service.student;

import com.example.codecool.dto.student.Average;
import com.example.codecool.dto.student.StudentAverageDto;
import com.example.codecool.entity.end.ExamEntity;
import com.example.codecool.entity.end.ResultEntity;
import com.example.codecool.entity.end.UserEntity;
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
public class AverageCalculatorService {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;

    private static final Logger logger = LogManager.getLogger(AverageCalculatorService.class);

    public AverageCalculatorService(UserRepository userRepository,
                                    ExamRepository examRepository) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
    }

    public List<StudentAverageDto> calculateAverage() {

        List<StudentAverageDto> response = new ArrayList<>();
        List<UserEntity> students = userRepository.findAllByIsStudent(true);
        logger.info("Retrieved {} students who are currently enrolled.", students.size());

        List<ExamEntity> exams = examRepository.findLatestNonCanceledClassesForAllStudents();
        logger.info("Retrieved {} non-canceled exams for all students.", exams.size());

        for (UserEntity student : students) {
            logger.debug("Processing average for student: {}", student.getEmail());
            StudentAverageDto studentAverageDto = new StudentAverageDto();
            studentAverageDto.setAverageList(new ArrayList<>());
            studentAverageDto.setEmail(student.getEmail());
            Map<String, List<Integer>> dimensionScoresMap = new HashMap<>();

            for (ExamEntity exam : exams) {
                if (exam.getStudent().equals(student.getEmail())) {
                    logger.debug("Found exam ID: {} for student: {}", exam.getId(), student.getEmail());
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
                studentAverageDto.getAverageList().add(new Average(dimension, average));
                logger.info("Calculated average for dimension: {} for student: {} is: {}", dimension, student.getEmail(), average);
            }

            response.add(studentAverageDto);
        }

        return response;


    }
}
