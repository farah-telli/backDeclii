package tn.example.backdeclitech.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "module", indexes = @Index(name = "idx_module_id", columnList = "id"))
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @NotBlank(message = "Le titre ne peut pas être vide")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String title;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String description;

    @Lob
    @Column(name = "image_url")
    private String imageUrl;

    @Size(max = 50, message = "Le jour ne peut pas dépasser 50 caractères")
    @Column(length = 50)
    private String jour;

    private boolean actif;
    private int EnrolledCount;
    private boolean annulation;

    @ManyToOne
    @JsonIgnoreProperties("modules")
    @JoinColumn(name = "space_id")
    private CoBuildSpace coBuildSpace;

    @ManyToMany(mappedBy = "module")
    @JsonIgnoreProperties("module")
    private List<User> instructors;

    @OneToMany(mappedBy = "module")
    @JsonIgnoreProperties("module")
    private List<ModuleSession> moduleSessions;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("module")
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "module")
    private List<FeedBack> feedbacks;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    private List<Reclamation> reclamations;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<News> newsList;
}