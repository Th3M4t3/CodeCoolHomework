package com.example.codecool.controller;

import com.example.codecool.dto.student.StudentAverageDto;
import com.example.codecool.dto.teacher.TeacherStatisticsDto;
import com.example.codecool.service.student.AverageCalculatorForStudentService;
import com.example.codecool.service.student.AverageCalculatorService;
import com.example.codecool.service.teacher.StatisticsCalculatorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "calculate-average")
public class StatisticsController {
    private final AverageCalculatorService averageCalculatorService;
    private final AverageCalculatorForStudentService averageCalculatorForSutdentService;
    private final StatisticsCalculatorService statisticsCalculatorService;

    private static final Logger logger = LogManager.getLogger(StatisticsController.class);

    public StatisticsController(AverageCalculatorService averageCalculatorService,
                                AverageCalculatorForStudentService averageCalculatorForSutdentService,
                                StatisticsCalculatorService statisticsCalculatorService) {
        this.averageCalculatorService = averageCalculatorService;
        this.averageCalculatorForSutdentService = averageCalculatorForSutdentService;
        this.statisticsCalculatorService = statisticsCalculatorService;
    }


    @GetMapping("/all-students")
    public List<StudentAverageDto> calculateAverageForEachStudent() {
        logger.info("Initiating calculation of average for each student.");
        List<StudentAverageDto> response = averageCalculatorService.calculateAverage();
        logger.info("Completed calculation of average for each student.");
        return response;
    }

    @GetMapping("/student/{id}")
    public StudentAverageDto calculateAverageForSingleStudent(@PathVariable Long id) {
        logger.info("Initiating calculation of average for student with ID: {}.", id);
        StudentAverageDto response = averageCalculatorForSutdentService.calculateAverageForStudent(id);
        logger.info("Completed calculation of average for student with ID: {}.", id);
        return response;
    }

    @GetMapping("/all-teachers")
    public List<TeacherStatisticsDto> calculateStatisticsForEachTeacher() {
        logger.info("Initiating calculation of statistics for each teacher");
        List<TeacherStatisticsDto> response = statisticsCalculatorService.statisticscalculator();
        logger.info("Completed calculation of statistics for each teacher");
        return response;
    }

}
