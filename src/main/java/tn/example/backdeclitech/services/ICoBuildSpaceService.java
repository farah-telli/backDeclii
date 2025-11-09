package tn.example.backdeclitech.services;

import tn.example.backdeclitech.entities.CoBuildSpace;

import java.util.List;

public interface ICoBuildSpaceService {
    CoBuildSpace save(CoBuildSpace space);
    List<CoBuildSpace> getAll();
    CoBuildSpace getById(Long id);
    CoBuildSpace update(Long id, CoBuildSpace space);
    void delete(Long id);

}  ;