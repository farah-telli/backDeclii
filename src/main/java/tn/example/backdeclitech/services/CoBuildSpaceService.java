package tn.example.backdeclitech.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.example.backdeclitech.entities.CoBuildSpace;
import tn.example.backdeclitech.entities.Module;
import tn.example.backdeclitech.repositories.CoBuildSpaceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoBuildSpaceService implements ICoBuildSpaceService {
    private final CoBuildSpaceRepository repository;

    @Override
    public CoBuildSpace save(CoBuildSpace space) {
        if (space.getModules() != null) {
            for (Module module : space.getModules()) {
                module.setCoBuildSpace(space);
            }
        }
        return repository.save(space);
    }

    @Override
    public List<CoBuildSpace> getAll() {
        List<CoBuildSpace> list = new ArrayList<>();
        repository.findAll().forEach(list::add);
        return list;
    }

    @Override
    public CoBuildSpace getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public CoBuildSpace update(Long id, CoBuildSpace space) {
        Optional<CoBuildSpace> optional = repository.findById(id);
        if (optional.isPresent()) {
            CoBuildSpace existing = optional.get();
            existing.setName(space.getName());
            existing.setAddress(space.getAddress());
            existing.setActif(space.isActif());

            return repository.save(existing);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
