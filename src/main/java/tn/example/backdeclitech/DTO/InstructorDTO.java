package tn.example.backdeclitech.DTO;


import java.util.List;

import lombok.Data;
import java.util.List;

@Data
public class InstructorDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private List<String> modules;
    private String lastSessionDate;

}
