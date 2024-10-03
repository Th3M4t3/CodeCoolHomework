package com.example.codecool.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStatisticsDto {
    private String email;
    private Map<Long,Long> stats;
}
