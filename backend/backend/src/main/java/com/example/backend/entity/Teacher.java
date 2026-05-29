package com.example.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.net.Inet4Address;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "teachers")
public class Teacher {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     Integer id;
     @OneToOne
     @MapsId
     @JoinColumn(name = "user_id")  // chứa cả user nên có thể map entity
     User user;
     String teacher_code;
     String department;
}
