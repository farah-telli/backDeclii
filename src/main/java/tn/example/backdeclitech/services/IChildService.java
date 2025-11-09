package tn.example.backdeclitech.services;

import tn.example.backdeclitech.DTO.ChildResponse;
import tn.example.backdeclitech.entities.User;

import java.util.List;

public interface IChildService {
    List<ChildResponse> getChildDTOsByParent(User parent);

}
