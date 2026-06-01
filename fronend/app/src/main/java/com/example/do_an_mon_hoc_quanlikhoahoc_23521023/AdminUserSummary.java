package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

public class AdminUserSummary {

    private int userId;
    private String userCode;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String role;
    private String status;

    private String dateOfBirth;
    private String department;

    public AdminUserSummary() {
    }

    public AdminUserSummary(
            int userId,
            String userCode,
            String username,
            String fullName,
            String email,
            String phone,
            String avatarUrl,
            String role,
            String status,
            String dateOfBirth,
            String department
    ) {
        this.userId = userId;
        this.userCode = userCode;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.status = status;
        this.dateOfBirth = dateOfBirth;
        this.department = department;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getDepartment() {
        return department;
    }
}