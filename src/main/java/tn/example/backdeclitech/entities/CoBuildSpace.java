package tn.example.backdeclitech.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoBuildSpace implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long spaceId;
    String name;
    String address;
    boolean actif;
    @OneToMany(mappedBy = "coBuildSpace", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"coBuildSpace", "module"})
    private List<Module> modules;

}

