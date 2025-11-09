package tn.example.backdeclitech.presence_managment.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.Mapping;

import tn.example.backdeclitech.entities.ModuleSession;
import tn.example.backdeclitech.presence_managment.dto.ModuleSessionDTO;

@Mapper(componentModel = "spring")
public interface ModuleSessionMapper {

    @Mappings({
        @Mapping(source = "id", target = "sessionId"),
        @Mapping(source = "date", target = "date"),
        @Mapping(source = "startTime", target = "startTime"),
        @Mapping(source = "endTime", target = "endTime"),
        @Mapping(source = "module.title", target = "moduleName")
    })
    ModuleSessionDTO toDTO(ModuleSession entity);

    @Mappings({
        @Mapping(source = "sessionId", target = "id"),
        @Mapping(source = "date", target = "date"),
        @Mapping(source = "startTime", target = "startTime"),
        @Mapping(source = "endTime", target = "endTime"),
        @Mapping(source = "moduleName", target = "module.title"),
        @Mapping(target = "active", ignore = true),
        @Mapping(target = "capcity", ignore = true),
        @Mapping(target = "coBuildSpace", ignore = true),
        @Mapping(target = "enrolledCount", ignore = true),
        @Mapping(target = "module", ignore = true),
        @Mapping(target = "presences", ignore = true),
        @Mapping(target = "reservations", ignore = true),
        @Mapping(target = "trancheAgeMax", ignore = true),
        @Mapping(target = "trancheAgeMin", ignore = true)
    })
    ModuleSession toEntity(ModuleSessionDTO dto);
}
