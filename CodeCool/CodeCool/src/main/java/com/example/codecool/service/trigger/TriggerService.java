package com.example.codecool.service.trigger;

import com.example.codecool.dto.trigger.TriggerDto;
import com.example.codecool.entity.end.ExamEntity;
import com.example.codecool.entity.end.ProcessedDataEntity;
import com.example.codecool.entity.end.ResultEntity;
import com.example.codecool.entity.end.UserEntity;
import com.example.codecool.entity.source.JsonEntity;
import com.example.codecool.repository.end.ExamRepository;
import com.example.codecool.repository.end.ProcessedDataRepository;
import com.example.codecool.repository.end.UserRepository;
import com.example.codecool.repository.source.JsonRepository;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TriggerService {

    private final JsonRepository jsonRepository;
    private final ProcessedDataRepository processedDataRepository;
    private final ObjectMapper objectMapper;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    private static final Logger logger = LogManager.getLogger(TriggerService.class);

    @Autowired
    public TriggerService(JsonRepository jsonRepository,
                          ProcessedDataRepository processedDataRepository,
                          ObjectMapper objectMapper,
                          ExamRepository examRepository,
                          UserRepository userRepository

    ) {
        this.jsonRepository = jsonRepository;
        this.processedDataRepository = processedDataRepository;
        this.objectMapper = objectMapper;
        this.examRepository = examRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TriggerDto trigger() {

        // Fetch all unprocessed data from the SourceDB
        logger.info("Fetching unprocessed JSON data.");
        List<JsonEntity> unprocessedJsonData = fetchUnprocessedJsonData();

        if (unprocessedJsonData.isEmpty()) {
            logger.info("No unprocessed JSON data found. Skipping further processing.");
            TriggerDto response = new TriggerDto();
            response.setFailedCount(0);
            response.setProcessedCount(0);
            response.setTotalJsonFound(0);
            return response;  // You can create an empty response object if necessary
        }

        logger.info("Fetched {} unprocessed JSON records.", unprocessedJsonData.size());

        // Map each unprocessed data to an entity object
        logger.info("Mapping unprocessed JSON data to ExamEntity objects.");
        Map<Long, ExamEntity> processedExamData = mapJsonDataToEntity(unprocessedJsonData);

        if (processedExamData.isEmpty()) {
            logger.info("No valid ExamEntity data after mapping. Skipping further processing.");
            TriggerDto response = new TriggerDto();
            response.setFailedCount(unprocessedJsonData.size());
            response.setProcessedCount(0);
            response.setTotalJsonFound(unprocessedJsonData.size());
            return response;
        }

        logger.info("Mapped {} JSON records to ExamEntity objects.", processedExamData.size());

        // Verify that all entities adhere to the constraints before saving them
        logger.info("Validating mapped ExamEntity objects.");
        Map<Long, ExamEntity> checkedExamData = checkMappedEntity(processedExamData);

        if (checkedExamData.isEmpty()) {
            logger.info("No valid ExamEntity data after validation. Skipping further processing.");
            TriggerDto response = new TriggerDto();
            response.setFailedCount(unprocessedJsonData.size());
            response.setProcessedCount(0);
            response.setTotalJsonFound(unprocessedJsonData.size());
            return response;  // You can create an empty response object if necessary
        }

        logger.info("Validated {} ExamEntity objects.", checkedExamData.size());

        // Divide the Map into entries for IsProcessedEntity and ExamEntity
        List<ExamEntity> examList = new ArrayList<>();
        List<ProcessedDataEntity> isProcessedIdList = new ArrayList<>();

        for (Map.Entry<Long, ExamEntity> mapEntry : checkedExamData.entrySet()) {
            examList.add(mapEntry.getValue());
            isProcessedIdList.add(new ProcessedDataEntity(mapEntry.getKey()));
        }

        // Save the new Exams and flag them as processed
        if (!examList.isEmpty()) {
            logger.info("Saving {} ExamEntity records to the database.", examList.size());
            examRepository.saveAll(examList);
        } else {
            logger.info("No valid ExamEntity records to save.");
        }

        if (!isProcessedIdList.isEmpty()) {
            logger.info("Saving {} ProcessedDataEntity records to the database.", isProcessedIdList.size());
            processedDataRepository.saveAll(isProcessedIdList);
        } else {
            logger.info("No ProcessedDataEntity records to save.");
        }

        // Generate a response and show the user the outcome of the processing
        TriggerDto triggerResponse = new TriggerDto();
        triggerResponse.setTotalJsonFound(unprocessedJsonData.size());
        triggerResponse.setProcessedCount(examList.size());
        triggerResponse.setFailedCount(triggerResponse.getTotalJsonFound() - triggerResponse.getProcessedCount());

        logger.info("Entity processing completed. Total: {}, Processed: {}, Failed: {}.",
                triggerResponse.getTotalJsonFound(),
                triggerResponse.getProcessedCount(),
                triggerResponse.getFailedCount());

        return triggerResponse;
    }

    private List<JsonEntity> fetchUnprocessedJsonData() {
        //Fetch all IDs of data that have already been processed, so we can avoid processing them again
        List<Long> alreadyProcessedDataList = processedDataRepository.findAllProcessedDataIds();


        //If the list of processed data is empty, add a single 0L to prevent SQL errors during execution
        if (alreadyProcessedDataList.isEmpty()) {
            alreadyProcessedDataList.add(0L);
        }

        //Use the custom JPA method to extract all unprocessed data from the sourceDB
        return jsonRepository.findAllByIdNotIn(alreadyProcessedDataList);
    }

    private Map<Long, ExamEntity> mapJsonDataToEntity(List<JsonEntity> jsonDataList) {
        Map<Long, ExamEntity> examEntities = new HashMap<>();
        // Map each JSON string to a ExamEntity object
        for (JsonEntity jsonData : jsonDataList) {
            // The try block is required for handling exceptions when using objectMapper
            try {
                ExamEntity examEntity = objectMapper.readValue(jsonData.getJson(), ExamEntity.class);
                // Check if the ExamEntity contains results; if so, set the examId for each result
                if (examEntity.getResults() != null) {
                    for (ResultEntity resultEntity : examEntity.getResults()) {
                        resultEntity.setExamId(examEntity);
                    }
                }
                // Store the newly mapped ExamEntity objects in a list
                examEntities.put(jsonData.getId(), examEntity);
            } catch (JsonParseException e) {

                logger.error("Error parsing JSON (JSON ID: {})",
                        jsonData.getId());

            } catch (JsonMappingException e) {

                logger.error("Error mapping JSON to ExamEntity. (JSON ID: {})",
                        jsonData.getId());

            } catch (IOException e) {

                logger.error("IO error while processing JSON (JSON ID: {})",
                        jsonData.getId());

            }
        }
        // Catch any errors that may occur during the mapping process
        return examEntities;
    }

    private Map<Long, ExamEntity> checkMappedEntity(Map<Long, ExamEntity> examMap) {

        List<Long> toRemoveIdList = new ArrayList<>();
        for (Map.Entry<Long, ExamEntity> mapEntry : examMap.entrySet()) {

            // 1. Each NOT NULL field should contain a value.
            if (mapEntry.getValue().getClassName() == null
                    || mapEntry.getValue().getTeacher() == null
                    || mapEntry.getValue().getStudent() == null
                    || mapEntry.getValue().getDate() == null
                    || mapEntry.getValue().getCancelled() == null
                    || mapEntry.getValue().getComment() == null) {
                logger.error("ExamEntity validation failed: Missing required NOT NULL fields for JSON (JSON ID: {})",
                        mapEntry.getKey());
                toRemoveIdList.add(mapEntry.getKey());
                continue;
            }

            // 2. Check if teacher email is actually connected to a teacher profile, also teacher profile should exist
            UserEntity teacherProfile = userRepository.findByEmail(mapEntry.getValue().getTeacher());
            if (teacherProfile == null) {
                logger.error("Validation failed: No user registered with the provided email: {} (JSON ID: {})",
                        mapEntry.getValue().getTeacher(),
                        mapEntry.getKey());
                toRemoveIdList.add(mapEntry.getKey());
                continue;
            } else if (teacherProfile.getIsStudent()) {
                logger.error("Validation failed: Email {} is not associated with a teacher profile (JSON ID: {})",
                        mapEntry.getValue().getTeacher(),
                        mapEntry.getKey());
                toRemoveIdList.add(mapEntry.getKey());
                continue;
            }

            // 3. Check if student email is actually connected to a student profile, also student profile should exist
            UserEntity studentProfile = userRepository.findByEmail(mapEntry.getValue().getStudent());
            if (studentProfile == null) {
                logger.error("Validation failed: No user registered with the provided email: {} (JSON ID: {})",
                        mapEntry.getValue().getStudent(),
                        mapEntry.getKey());
                toRemoveIdList.add(mapEntry.getKey());
                continue;
            } else if (!studentProfile.getIsStudent()) {
                logger.error("Validation failed: Email {} is not associated with a student profile (JSON ID: {})",
                        mapEntry.getValue().getStudent(),
                        mapEntry.getKey());
                toRemoveIdList.add(mapEntry.getKey());
                continue;
            }

            // 4. Check if the exam was cancelled if it was than there should be no results and success is null
            if (mapEntry.getValue().getCancelled()) {
                if (mapEntry.getValue().getResults() != null) {
                    logger.error("Data inconsistency: Cancelled exam contains results. (JSON ID: {})",
                            mapEntry.getKey());
                    toRemoveIdList.add(mapEntry.getKey());
                }

                if (mapEntry.getValue().getSuccess() != null) {
                    logger.error("Data inconsistency: Cancelled exam contains a non-null success field. (JSON ID: {})",
                            mapEntry.getKey());
                    toRemoveIdList.add(mapEntry.getKey());
                }
            }
        }
        examMap.entrySet().removeIf(entry -> toRemoveIdList.contains(entry.getKey()));
        return examMap;
    }
}
