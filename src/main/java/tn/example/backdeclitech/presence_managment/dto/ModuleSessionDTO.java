package tn.example.backdeclitech.presence_managment.dto;

import java.time.LocalTime;
import java.util.Date;

import lombok.Data;

@Data
public class ModuleSessionDTO {
    private Long sessionId;
    private Date date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String moduleName;
}
