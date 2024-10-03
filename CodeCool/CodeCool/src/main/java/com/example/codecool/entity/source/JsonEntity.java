package com.example.codecool.entity.source;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor

@Entity(name = "json")
public class JsonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",
            nullable = false
    )
    private Long id;
    @Column(name = "json",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String json;

    public JsonEntity(String json) {
        this.json = json;
    }
}

