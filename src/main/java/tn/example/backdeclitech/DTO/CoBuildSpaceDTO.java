package tn.example.backdeclitech.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CoBuildSpaceDTO {
    // Getters et Setters
    private long spaceId;
    private String name;
    private String address;
    private boolean actif;

    public CoBuildSpaceDTO() {}

    public CoBuildSpaceDTO(long spaceId, String name, String address, boolean actif) {
        this.spaceId = spaceId;
        this.name = name;
        this.address = address;
        this.actif = actif;
    }

}
