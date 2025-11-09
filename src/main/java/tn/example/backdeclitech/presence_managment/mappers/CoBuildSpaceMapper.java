package tn.example.backdeclitech.presence_managment.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

import tn.example.backdeclitech.DTO.CoBuildSpaceDTO;
import tn.example.backdeclitech.entities.CoBuildSpace;

import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CoBuildSpaceMapper {

    @Mappings({
        @Mapping(source = "spaceId", target = "spaceId"),
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "address", target = "address")
    })
    CoBuildSpaceDTO toDTO(CoBuildSpace entity);

    @Mappings({
        @Mapping(source = "spaceId", target = "spaceId"),
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "address", target = "address"),
        @Mapping(target = "modules", ignore = true)
    })
    CoBuildSpace toEntity(CoBuildSpaceDTO dto);
}
