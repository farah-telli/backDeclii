package tn.example.backdeclitech.entities;

import jakarta.validation.constraints.NotBlank;

public class PhoneDTO {
    @NotBlank
    private String phoneNumber;

    public @NotBlank String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NotBlank String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
