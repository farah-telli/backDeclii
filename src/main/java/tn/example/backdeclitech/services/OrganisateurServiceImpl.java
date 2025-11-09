package tn.example.backdeclitech.services;

import org.springframework.stereotype.Service;
import tn.example.backdeclitech.DTO.OrganisateurPresenceDto;
import tn.example.backdeclitech.DTO.Page;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganisateurServiceImpl implements OrganisateurService {

    @Override
    public Page<OrganisateurPresenceDto> getAttendance(
            String type,
            String searchTerm,
            String selectedCoBuildSpace,
            String selectedTitre,
            String startDate,
            String endDate,
            int page,
            int size
    ) {
        // Mock data pour tester Angular
        List<OrganisateurPresenceDto> list = new ArrayList<>();

        OrganisateurPresenceDto a = new OrganisateurPresenceDto();
        a.setChildName("Ali");
        a.setParentName("Meriem");
        a.setParentPhone("12345678");
        a.setModuleName("Code Combat");
        a.setCobuildSpaceName("CoBuild Ariana");
        a.setSessionTime("08:00");
        try {
            a.setSessionDate(new SimpleDateFormat("yyyy-MM-dd").parse("2025-10-28"));
        } catch (Exception e) {}
        a.setStatus(type.equals("absence") ? "Absence" : "Présent");

        list.add(a);

        Page<OrganisateurPresenceDto> pageResult = new Page<>();
        pageResult.setContent(list);
        pageResult.setNumber(page);
        pageResult.setSize(list.size());
        pageResult.setTotalElements(list.size());
        pageResult.setTotalPages(1);
        pageResult.setNumberOfElements(list.size());
        pageResult.setFirst(true);
        pageResult.setLast(true);
        pageResult.setEmpty(list.isEmpty());

        return pageResult;
    }
}
