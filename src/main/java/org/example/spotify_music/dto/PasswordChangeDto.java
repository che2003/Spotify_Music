package org.example.spotify_music.dto;

public class PasswordChangeDto {
    private String oldPassword;
    private String newPassword;

    // Getter/Setter
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}