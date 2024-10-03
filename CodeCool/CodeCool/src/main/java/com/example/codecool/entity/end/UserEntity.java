package com.example.codecool.entity.end;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "profile")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",
            nullable = false
    )
    private Long id;
    @Column(name = "name",
            nullable = false,
            columnDefinition = "VARCHAR(255)"
    )
    private String name;

    @Column(name = "email",
            nullable = false,
            columnDefinition = "VARCHAR(255)",
            unique = true
    )
    private String email;
    @Column(name = "birthday",
            nullable = false,
            columnDefinition = "DATE"
    )
    private LocalDate birthday;
    @Column(name = "is_student",
            nullable = false,
            columnDefinition = "BOOLEAN"
    )
    private Boolean isStudent;

    public UserEntity(String name,
                      String email,
                      LocalDate birthday,
                      boolean isStudent) {
        this.name = name;
        this.email = email;
        this.birthday = birthday;
        this.isStudent = isStudent;
    }
}
