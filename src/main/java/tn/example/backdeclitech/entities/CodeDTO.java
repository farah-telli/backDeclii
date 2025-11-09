package tn.example.backdeclitech.entities;

import jakarta.validation.constraints.NotBlank;

public class CodeDTO {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String Code;
    @NotBlank
    private String confirmCode;

    public @NotBlank String getCode() {
        return Code;
    }

    public void setCode(@NotBlank String code) {
        Code = code;
    }

    public @NotBlank String getConfirmCode() {
        return confirmCode;
    }

    public void setConfirmCode(@NotBlank String confirmCode) {
        this.confirmCode = confirmCode;
    }

    public @NotBlank String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NotBlank String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
