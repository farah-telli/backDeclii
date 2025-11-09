package tn.example.backdeclitech.presence_managment.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import tn.example.backdeclitech.presence_managment.dto.SiteExitPresenceDTO;
import tn.example.backdeclitech.presence_managment.entities.SiteExitPresence;

@Mapper(componentModel = "spring", uses = { PresenceMapper.class })
public interface SiteExitPresenceMapper {

    /**
     * Converts a SiteExitPresence entity to a SiteExitPresenceDTO object.
     *
     * @param entity the SiteExitPresence entity to convert
     * @return the converted SiteExitPresenceDTO object
     */
    @Mapping(source = "presence", target = "presence")
    SiteExitPresenceDTO toDTO(SiteExitPresence entity);

    /**
     * Converts a SiteExitPresenceDTO object to a SiteExitPresence entity.
     *
     * @param dto the SiteExitPresenceDTO to convert
     * @return the converted SiteExitPresence entity
     */
    @Mappings({
            @Mapping(source = "presence", target = "presence"),
            @Mapping(target = "parentSignature", ignore = true),
    })
    SiteExitPresence toEntity(SiteExitPresenceDTO dto);
}