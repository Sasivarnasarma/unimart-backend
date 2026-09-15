package varna.mit.kln.unimart.auth.dto;

import java.time.LocalDateTime;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.entity.UserRole;

public class UserResponseDto {

    private Integer id;
    private String universityEmail;
    private String fullName;
    private UserRole role;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponseDto() {
    }

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.universityEmail = user.getUniversityEmail();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.emailVerified = user.getEmailVerified();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUniversityEmail() {
        return universityEmail;
    }

    public void setUniversityEmail(String universityEmail) {
        this.universityEmail = universityEmail;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
