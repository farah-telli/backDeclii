package tn.example.backdeclitech.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 5000)
    private String content;

    private String imageUrl;

    private LocalDateTime createdAt;

    private String type;

    @ManyToOne
    @JsonBackReference
    private Module module;

    @Transient
    @JsonProperty("fullImageUrl")
    public String getFullImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return "http://localhost:8089/uploads/news/" + imageUrl;
        }
        return null;
    }
}