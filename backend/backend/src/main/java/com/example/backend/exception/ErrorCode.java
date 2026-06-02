package com.example.backend.exception;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(1001, "User Not Found"),
    UN_AUTHENTICATED(1002, "Cannot Authenticate"),
    PASSWORD_ERROR(1003,"Passsword Error" ),
    PASSWORD_CHECK(1004,"New Password and Old Password are the same" ),
    LESSON_NOT_FOUND(1005, "Lesson not found" ),
    MODULE_NOT_FOUND(1006, "module not found" ),
    USER_DELETED(1007, "User deleted" );
    private int code;
    private String message;

}
