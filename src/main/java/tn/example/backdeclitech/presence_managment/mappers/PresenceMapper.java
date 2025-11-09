package tn.example.backdeclitech.presence_managment.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import tn.example.backdeclitech.presence_managment.dto.PresenceDTO;
import tn.example.backdeclitech.presence_managment.entities.Presence;

@Mapper(componentModel = "spring",uses ={ ModuleSessionMapper.class, CoBuildSpaceMapper.class })
public interface PresenceMapper {

    @Mappings({
        @Mapping(source = "updateTime", target = "dateTime"),
        @Mapping(source = "reservationDate", target = "reservationDate"),
        @Mapping(source = "pupilName", target = "pupilName"),
        @Mapping(source = "present", target = "present"),
        @Mapping(source = "parentName", target = "parentName"),
        @Mapping(source = "lastUpdated", target = "lastUpdated"),
        @Mapping(source = "child.id", target = "childId"),
        @Mapping(source = "organizingTeam.id", target = "organizingTeamId"),
        @Mapping(source = "parent.id", target = "parentId"),
        @Mapping(source = "status", target = "status")
    })
    PresenceDTO toDTO(Presence presence);
    
    @Mappings({
        @Mapping(source = "dateTime", target = "updateTime"),
        @Mapping(source = "reservationDate", target = "reservationDate"),
        @Mapping(source = "pupilName", target = "pupilName"),
        @Mapping(source = "present", target = "present"),
        @Mapping(source = "parentName", target = "parentName"),
        @Mapping(source = "lastUpdated", target = "lastUpdated"),
        @Mapping(source = "childId", target = "child.id"),
        @Mapping(target = "moduleSessionPresence", ignore = true),
        @Mapping(source = "organizingTeamId", target = "organizingTeam.id"),
        @Mapping(source = "parentId", target = "parent.id"),
        @Mapping(target = "siteEntryPresence", ignore = true),
        @Mapping(target = "siteExitPresence", ignore = true),
        @Mapping(source = "status", target = "status")
    })
    Presence toEntity(PresenceDTO dto);
}
