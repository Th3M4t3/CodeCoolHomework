package com.example.codecool.dto.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TriggerDto {

    private int totalJsonFound;

    private int processedCount;

    private int failedCount;

}
