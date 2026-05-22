package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @ManyToOne
    @MapsId
    @JoinColumn(name = "lesson_id")
    Lesson lesson;
    String title;
    String objective;
    String content;
    String example;
    String image_example_url;
    Integer order_index;
    Integer status;
}
