package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.example.backdeclitech.entities.Sentiment;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponse {
    private Sentiment sentiment;

    private int id;
    private String content;
    private int rating;
    private LocalDateTime submittedAt;
    private String parentName;
    private String parentPhone;
    private Long parentId;
    private String moduleName;
    private Long moduleId;
    private Long reservationId;
    private String childName;

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }
}