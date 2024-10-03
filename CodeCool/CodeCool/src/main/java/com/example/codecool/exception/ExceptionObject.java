package com.example.codecool.exception;


import lombok.Data;

import java.util.Date;

@Data
public class ExceptionObject {

    private Integer statusCode;

    private String message;

    private Date timestamp;

}
