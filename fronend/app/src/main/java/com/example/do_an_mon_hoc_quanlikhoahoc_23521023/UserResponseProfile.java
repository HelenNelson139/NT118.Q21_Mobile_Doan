package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

public class UserResponseProfile {
    // Ép kiểu ID về String hoặc Integer tùy thuộc vào kiểu dữ liệu ID thực tế của bảng User trong Entity Backend của bạn
    private String username;
    private String email;
    private String phone;
    private String fullName;  // Tương ứng với full_name ở BE
    private String avatarUrl; // Tương ứng với avatar_url ở BE
    private String status;

    // Getters
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getStatus() { return status; }
}