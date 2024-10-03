package com.example.codecool.trigger;

import com.example.codecool.dto.trigger.TriggerDto;
import com.example.codecool.entity.end.ExamEntity;
import com.example.codecool.entity.end.ResultEntity;
import com.example.codecool.entity.end.UserEntity;
import com.example.codecool.entity.source.JsonEntity;
import com.example.codecool.repository.end.ExamRepository;
import com.example.codecool.repository.end.ProcessedDataRepository;
import com.example.codecool.repository.end.UserRepository;
import com.example.codecool.repository.source.JsonRepository;
import com.example.codecool.service.trigger.TriggerService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class TriggerServiceTest {

    private TriggerService triggerService;

    @Mock
    private JsonRepository jsonRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ProcessedDataRepository processedDataRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserRepository userRepository;

    private AutoCloseable autoCloseable;


    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        triggerService = new TriggerService(jsonRepository,
                                            processedDataRepository,
                                            objectMapper,
                                            examRepository,
                                            userRepository);
    }
    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    //---------------- generic tests ----------------
    @Test
    @DisplayName("Correct data test")
    public void testTriggerSuccessfulProcessing() throws JsonProcessingException {

        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setId(1L);
        jsonEntity.setJson("{ \"class\": \"Programming Basics\", \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-02-01\", \"cancelled\": true, \"comment\": \"Foo was sick.\" }");

        ExamEntity examEntity = new ExamEntity();
        examEntity.setClassName("Programming Basics");
        examEntity.setTeacher("aaa@codecool.com");
        examEntity.setStudent("foo@bar.com");
        examEntity.setDate(LocalDate.parse("2024-02-01"));
        examEntity.setCancelled(true);
        examEntity.setComment("Foo was sick.");

        UserEntity studentUserEntity = new UserEntity();
        studentUserEntity.setId(1L);
        studentUserEntity.setIsStudent(true);
        studentUserEntity.setEmail("foo@bar.com");
        studentUserEntity.setName("Mr. Foo");
        studentUserEntity.setBirthday(LocalDate.parse("2024-02-01"));

        UserEntity teacherUserEntity = new UserEntity();
        teacherUserEntity.setId(2L);
        teacherUserEntity.setIsStudent(false);
        teacherUserEntity.setEmail("aaa@codecool.com");
        teacherUserEntity.setName("Mr. Aaa");
        teacherUserEntity.setBirthday(LocalDate.parse("2024-02-01"));
        
        List<JsonEntity> unprocessedJsonData = Collections.singletonList(jsonEntity);

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(unprocessedJsonData);
        when(objectMapper.readValue(jsonEntity.getJson(), ExamEntity.class)).thenReturn(examEntity);
        when(examRepository.saveAll(anyList())).thenReturn(Collections.singletonList(examEntity));
        when(processedDataRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(userRepository.findByEmail(examEntity.getStudent())).thenReturn(studentUserEntity);
        when(userRepository.findByEmail(examEntity.getTeacher())).thenReturn(teacherUserEntity);

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(1, response.getTotalJsonFound());
        assertEquals(1, response.getProcessedCount());
        assertEquals(0, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(objectMapper).readValue(jsonEntity.getJson(), ExamEntity.class);
        verify(examRepository).saveAll(anyList());
        verify(processedDataRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("No data test")
    public void testTriggerWithEmptyJsonData() {

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(Collections.emptyList());

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(0, response.getTotalJsonFound());
        assertEquals(0, response.getProcessedCount());
        assertEquals(0, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(examRepository, never()).saveAll(anyList());
        verify(processedDataRepository, never()).saveAll(anyList());
    }

    //---------------- ObjectMapper Exception tests ----------------

    @Test
    @DisplayName("JsonParseException test")
    public void testJsonParseException() throws Exception {

        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setId(1L);
        jsonEntity.setJson("{ invalid json ");

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(Collections.singletonList(jsonEntity));
        when(objectMapper.readValue(jsonEntity.getJson(), ExamEntity.class)).thenThrow(new JsonParseException(null, "Invalid JSON"));

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(1, response.getTotalJsonFound());
        assertEquals(0, response.getProcessedCount());
        assertEquals(1, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(objectMapper).readValue(jsonEntity.getJson(), ExamEntity.class);
        verify(examRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("JsonMappingException test")
    public void testJsonMappingException() throws Exception {

        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setId(2L);
        jsonEntity.setJson("{ \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\" }");

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(Collections.singletonList(jsonEntity));
        when(objectMapper.readValue(jsonEntity.getJson(), ExamEntity.class)).thenThrow(new JsonMappingException(null, "Mapping error"));

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(1, response.getTotalJsonFound());
        assertEquals(0, response.getProcessedCount());
        assertEquals(1, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(objectMapper).readValue(jsonEntity.getJson(), ExamEntity.class);
        verify(examRepository, never()).saveAll(anyList());
    }

    //---------------- Constraint tests ----------------
    // 1. Each NOT NULL field should contain a value.

    @Test
    @DisplayName("Not All data test")
    public void testNotNullConstraint() throws JsonProcessingException {

        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setId(1L);
        jsonEntity.setJson("{ \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-02-01\", \"cancelled\": true, \"comment\": \"Foo was sick.\" }");

        ExamEntity examEntity = new ExamEntity();
        examEntity.setClassName(null);
        examEntity.setTeacher("aaa@codecool.com");
        examEntity.setStudent("foo@bar.com");
        examEntity.setDate(LocalDate.parse("2024-02-01"));
        examEntity.setCancelled(true);
        examEntity.setComment("Foo was sick.");

        UserEntity studentUserEntity = new UserEntity();
        studentUserEntity.setId(1L);
        studentUserEntity.setIsStudent(true);
        studentUserEntity.setEmail("foo@bar.com");
        studentUserEntity.setName("Mr. Foo");
        studentUserEntity.setBirthday(LocalDate.parse("2024-02-01"));

        UserEntity teacherUserEntity = new UserEntity();
        teacherUserEntity.setId(2L);
        teacherUserEntity.setIsStudent(false);
        teacherUserEntity.setEmail("aaa@codecool.com");
        teacherUserEntity.setName("Mr. Aaa");
        teacherUserEntity.setBirthday(LocalDate.parse("2024-02-01"));


        List<JsonEntity> unprocessedJsonData = Collections.singletonList(jsonEntity);

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(unprocessedJsonData);
        when(objectMapper.readValue(jsonEntity.getJson(), ExamEntity.class)).thenReturn(examEntity);

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(1, response.getTotalJsonFound());
        assertEquals(0, response.getProcessedCount());
        assertEquals(1, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(objectMapper).readValue(jsonEntity.getJson(), ExamEntity.class);
    }

    // 2. Check if teacher email is actually connected to a teacher profile, also teacher profile should exist

    @Test
    @DisplayName("Teacher email not connected to teacher user test")
    public void testTeacherNotConnectedToTeacherUserEmail() throws JsonProcessingException {

        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setId(1L);
        jsonEntity.setJson("{ \"class\": \"Programming Basics\", \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-02-01\", \"cancelled\": true, \"comment\": \"Foo was sick.\" }");

        ExamEntity examEntity = new ExamEntity();
        examEntity.setClassName("Programming Basics");
        examEntity.setTeacher("aaa@codecool.com");
        examEntity.setStudent("foo@bar.com");
        examEntity.setDate(LocalDate.parse("2024-02-01"));
        examEntity.setCancelled(true);
        examEntity.setComment("Foo was sick.");

        UserEntity studentUserEntity = new UserEntity();
        studentUserEntity.setId(1L);
        studentUserEntity.setIsStudent(true);
        studentUserEntity.setEmail("foo@bar.com");
        studentUserEntity.setName("Mr. Foo");
        studentUserEntity.setBirthday(LocalDate.parse("2024-02-01"));

        UserEntity teacherUserEntity = new UserEntity();
        teacherUserEntity.setId(2L);
        teacherUserEntity.setIsStudent(true);
        teacherUserEntity.setEmail("aaa@codecool.com");
        teacherUserEntity.setName("Mr. Aaa");
        teacherUserEntity.setBirthday(LocalDate.parse("2024-02-01"));

        List<JsonEntity> unprocessedJsonData = Collections.singletonList(jsonEntity);

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(unprocessedJsonData);
        when(objectMapper.readValue(jsonEntity.getJson(), ExamEntity.class)).thenReturn(examEntity);
        when(userRepository.findByEmail(examEntity.getTeacher())).thenReturn(teacherUserEntity);

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(1, response.getTotalJsonFound());
        assertEquals(0, response.getProcessedCount());
        assertEquals(1, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(objectMapper).readValue(jsonEntity.getJson(), ExamEntity.class);
    }

    @Test
    @DisplayName("Teacher email not connected to any user test")
    public void testTeacherNotConnectedToAnyUserEmail() throws JsonProcessingException {

        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setId(1L);
        jsonEntity.setJson("{ \"class\": \"Programming Basics\", \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-02-01\", \"cancelled\": true, \"comment\": \"Foo was sick.\" }");

        ExamEntity examEntity = new ExamEntity();
        examEntity.setClassName("Programming Basics");
        examEntity.setTeacher("aaa@codecool.com");
        examEntity.setStudent("foo@bar.com");
        examEntity.setDate(LocalDate.parse("2024-02-01"));
        examEntity.setCancelled(true);
        examEntity.setComment("Foo was sick.");

        UserEntity studentUserEntity = new UserEntity();
        studentUserEntity.setId(1L);
        studentUserEntity.setIsStudent(true);
        studentUserEntity.setEmail("foo@bar.com");
        studentUserEntity.setName("Mr. Foo");
        studentUserEntity.setBirthday(LocalDate.parse("2024-02-01"));

        List<JsonEntity> unprocessedJsonData = Collections.singletonList(jsonEntity);

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(unprocessedJsonData);
        when(objectMapper.readValue(jsonEntity.getJson(), ExamEntity.class)).thenReturn(examEntity);
        when(userRepository.findByEmail(examEntity.getTeacher())).thenReturn(null);

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(1, response.getTotalJsonFound());
        assertEquals(0, response.getProcessedCount());
        assertEquals(1, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(objectMapper).readValue(jsonEntity.getJson(), ExamEntity.class);
    }

    // 3. Check if student email is actually connected to a student profile, also student profile should exist
    @Test
    @DisplayName("Student email not connected to teacher user test")
    public void testStudentNotConnectedToTeacherUserEmail() throws JsonProcessingException {

        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setId(1L);
        jsonEntity.setJson("{ \"class\": \"Programming Basics\", \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-02-01\", \"cancelled\": true, \"comment\": \"Foo was sick.\" }");

        ExamEntity examEntity = new ExamEntity();
        examEntity.setClassName("Programming Basics");
        examEntity.setTeacher("aaa@codecool.com");
        examEntity.setStudent("foo@bar.com");
        examEntity.setDate(LocalDate.parse("2024-02-01"));
        examEntity.setCancelled(true);
        examEntity.setComment("Foo was sick.");

        UserEntity studentUserEntity = new UserEntity();
        studentUserEntity.setId(1L);
        studentUserEntity.setIsStudent(false);
        studentUserEntity.setEmail("foo@bar.com");
        studentUserEntity.setName("Mr. Foo");
        studentUserEntity.setBirthday(LocalDate.parse("2024-02-01"));

        UserEntity teacherUserEntity = new UserEntity();
        teacherUserEntity.setId(2L);
        teacherUserEntity.setIsStudent(false);
        teacherUserEntity.setEmail("aaa@codecool.com");
        teacherUserEntity.setName("Mr. Aaa");
        teacherUserEntity.setBirthday(LocalDate.parse("2024-02-01"));


        List<JsonEntity> unprocessedJsonData = Collections.singletonList(jsonEntity);

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(unprocessedJsonData);
        when(objectMapper.readValue(jsonEntity.getJson(), ExamEntity.class)).thenReturn(examEntity);
        when(userRepository.findByEmail(examEntity.getTeacher())).thenReturn(teacherUserEntity);

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(1, response.getTotalJsonFound());
        assertEquals(0, response.getProcessedCount());
        assertEquals(1, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(objectMapper).readValue(jsonEntity.getJson(), ExamEntity.class);
    }

    @Test
    @DisplayName("Student email not connected to any user test")
    public void testStudentNotConnectedToAnyUserEmail() throws JsonProcessingException {
        // Create a sample JsonEntity
        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setId(1L);
        jsonEntity.setJson("{ \"class\": \"Programming Basics\", \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-02-01\", \"cancelled\": true, \"comment\": \"Foo was sick.\" }");

        ExamEntity examEntity = new ExamEntity();
        examEntity.setClassName("Programming Basics");
        examEntity.setTeacher("aaa@codecool.com");
        examEntity.setStudent("foo@bar.com");
        examEntity.setDate(LocalDate.parse("2024-02-01"));
        examEntity.setCancelled(true);
        examEntity.setComment("Foo was sick.");

        UserEntity teacherUserEntity = new UserEntity();
        teacherUserEntity.setId(1L);
        teacherUserEntity.setIsStudent(false);
        teacherUserEntity.setEmail("aaa@codecool.com");
        teacherUserEntity.setName("Mr. Aaa");
        teacherUserEntity.setBirthday(LocalDate.parse("2024-02-01"));

        List<JsonEntity> unprocessedJsonData = Collections.singletonList(jsonEntity);

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(unprocessedJsonData);
        when(objectMapper.readValue(jsonEntity.getJson(), ExamEntity.class)).thenReturn(examEntity);
        when(userRepository.findByEmail(examEntity.getStudent())).thenReturn(null);
        when(userRepository.findByEmail(examEntity.getTeacher())).thenReturn(teacherUserEntity);

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(1, response.getTotalJsonFound());
        assertEquals(0, response.getProcessedCount());
        assertEquals(1, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(objectMapper).readValue(jsonEntity.getJson(), ExamEntity.class);
    }

    // 4. Check if the exam was cancelled if it was than there should be no results and success is null

    @Test
    @DisplayName("Exam was cancelled yet success is not null test")
    public void testExamWasCanceledYetSuccesIsNotNull() throws JsonProcessingException {

        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setId(1L);
        jsonEntity.setJson("{ \"class\": \"Programming Basics\", \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-02-05\", \"cancelled\": true, \"success\": true, \"comment\": \"Everything was ok.\"");

        ExamEntity examEntity = new ExamEntity();
        examEntity.setClassName("Programming Basics");
        examEntity.setTeacher("aaa@codecool.com");
        examEntity.setStudent("foo@bar.com");
        examEntity.setDate(LocalDate.parse("2024-02-05"));
        examEntity.setCancelled(true);
        examEntity.setSuccess(Boolean.FALSE);
        examEntity.setComment("Everything was ok.");

        UserEntity studentUserEntity = new UserEntity();
        studentUserEntity.setId(1L);
        studentUserEntity.setIsStudent(true);
        studentUserEntity.setEmail("foo@bar.com");
        studentUserEntity.setName("Mr. Foo");
        studentUserEntity.setBirthday(LocalDate.parse("2024-02-01"));

        UserEntity teacherUserEntity = new UserEntity();
        teacherUserEntity.setId(2L);
        teacherUserEntity.setIsStudent(false);
        teacherUserEntity.setEmail("aaa@codecool.com");
        teacherUserEntity.setName("Mr. Aaa");
        teacherUserEntity.setBirthday(LocalDate.parse("2024-02-01"));


        List<JsonEntity> unprocessedJsonData = Collections.singletonList(jsonEntity);

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(unprocessedJsonData);
        when(objectMapper.readValue(jsonEntity.getJson(), ExamEntity.class)).thenReturn(examEntity);
        when(userRepository.findByEmail(examEntity.getStudent())).thenReturn(studentUserEntity);
        when(userRepository.findByEmail(examEntity.getTeacher())).thenReturn(teacherUserEntity);

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(1, response.getTotalJsonFound());
        assertEquals(0, response.getProcessedCount());
        assertEquals(1, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(objectMapper).readValue(jsonEntity.getJson(), ExamEntity.class);
    }

    @Test
    @DisplayName("Exam was cancelled yet there are results test")
    public void testExamWasCanceledYetThereIsResult() throws JsonProcessingException {

        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setId(1L);
        jsonEntity.setJson("{ \"class\": \"Programming Basics\", \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-02-05\", \"cancelled\": true, \"success\": true, \"comment\": \"Everything was ok.\", \"results\": [{ \"dimension\": \"Coding\", \"result\": 80 }, { \"dimension\": \"Communication\", \"result\": 100}] }");

        ExamEntity examEntity = new ExamEntity();
        examEntity.setId(1L);
        examEntity.setClassName("Programming Basics");
        examEntity.setTeacher("aaa@codecool.com");
        examEntity.setStudent("foo@bar.com");
        examEntity.setDate(LocalDate.parse("2024-02-05"));
        examEntity.setCancelled(true);
        examEntity.setComment("Everything was ok.");

        List<ResultEntity> resultEntityList = new ArrayList<>();
        resultEntityList.add(new ResultEntity(examEntity,"Coding",80));
        resultEntityList.add(new ResultEntity(examEntity,"Communication",100));

        examEntity.setResults(resultEntityList);

        UserEntity studentUserEntity = new UserEntity();
        studentUserEntity.setId(1L);
        studentUserEntity.setIsStudent(true);
        studentUserEntity.setEmail("foo@bar.com");
        studentUserEntity.setName("Mr. Foo");
        studentUserEntity.setBirthday(LocalDate.parse("2024-02-01"));

        UserEntity teacherUserEntity = new UserEntity();
        teacherUserEntity.setId(2L);
        teacherUserEntity.setIsStudent(false);
        teacherUserEntity.setEmail("aaa@codecool.com");
        teacherUserEntity.setName("Mr. Aaa");
        teacherUserEntity.setBirthday(LocalDate.parse("2024-02-01"));


        List<JsonEntity> unprocessedJsonData = Collections.singletonList(jsonEntity);

        when(jsonRepository.findAllByIdNotIn(anyList())).thenReturn(unprocessedJsonData);
        when(objectMapper.readValue(jsonEntity.getJson(), ExamEntity.class)).thenReturn(examEntity);
        when(userRepository.findByEmail(examEntity.getStudent())).thenReturn(studentUserEntity);
        when(userRepository.findByEmail(examEntity.getTeacher())).thenReturn(teacherUserEntity);

        TriggerDto response = triggerService.trigger();

        assertNotNull(response);
        assertEquals(1, response.getTotalJsonFound());
        assertEquals(0, response.getProcessedCount());
        assertEquals(1, response.getFailedCount());

        verify(jsonRepository).findAllByIdNotIn(anyList());
        verify(objectMapper).readValue(jsonEntity.getJson(), ExamEntity.class);
    }
}
