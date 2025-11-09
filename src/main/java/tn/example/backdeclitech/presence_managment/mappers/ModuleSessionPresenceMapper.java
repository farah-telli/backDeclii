package tn.example.backdeclitech.presence_managment.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import tn.example.backdeclitech.presence_managment.dto.ModuleSessionPresenceDTO;
import tn.example.backdeclitech.presence_managment.entities.ModuleSessionPresence;

@Mapper(componentModel = "spring", uses = {PresenceMapper.class})
public interface ModuleSessionPresenceMapper {

    @Mapping(source = "presence", target = "presence")
    ModuleSessionPresenceDTO toDTO(ModuleSessionPresence entity);

    @Mapping(source = "presence", target = "presence")
    ModuleSessionPresence toEntity(ModuleSessionPresenceDTO dto);
}