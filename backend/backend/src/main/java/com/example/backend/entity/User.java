package com.example.backend.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.example.backend.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     Integer id;
     String username;
     String phone;
     String email;
     String password;
     String full_name;
     Role role;
     String status;
     String avatar_url;
     @CreatedDate
     @Column(updatable = false, nullable = false)
     Date created_at;
     @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
     Student student;
     @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
     @JsonIgnore
     Teacher teacher;
     @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
     Admin admin;

}
