package tn.example.backdeclitech.presence_managment.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import tn.example.backdeclitech.presence_managment.dto.SiteEntryPresenceDTO;
import tn.example.backdeclitech.presence_managment.entities.SiteEntryPresence;

@Mapper(componentModel = "spring", uses = {PresenceMapper.class})
public interface SiteEntryPresenceMapper {


    @Mapping(source = "presence", target = "presence")
    @Mapping(target = "supplementaryInfo", source = "supplementaryInfo")
    @Mapping(target = "disciplineReport", source = "disciplineReport")
    SiteEntryPresenceDTO toDTO(SiteEntryPresence entity);

    @Mapping(source = "presence", target = "presence")
    @Mapping(target = "supplementaryInfo", source = "supplementaryInfo")
    @Mapping(target = "disciplineReport", source = "disciplineReport")
    SiteEntryPresence toEntity(SiteEntryPresenceDTO dto);
}