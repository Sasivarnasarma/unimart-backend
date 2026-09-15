package varna.mit.kln.unimart.auth.dto;

public class LoginResponseDto {

    private String token;
    private Integer expiresInMinutes;
    private UserResponseDto user;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String token, Integer expiresInMinutes, UserResponseDto user) {
        this.token = token;
        this.expiresInMinutes = expiresInMinutes;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getExpiresInMinutes() {
        return expiresInMinutes;
    }

    public void setExpiresInMinutes(Integer expiresInMinutes) {
        this.expiresInMinutes = expiresInMinutes;
    }

    public UserResponseDto getUser() {
        return user;
    }

    public void setUser(UserResponseDto user) {
        this.user = user;
    }
}
