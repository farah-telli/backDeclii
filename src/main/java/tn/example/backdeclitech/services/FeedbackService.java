package tn.example.backdeclitech.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.example.backdeclitech.DTO.FeedbackRequest;
import tn.example.backdeclitech.DTO.FeedbackResponse;
import tn.example.backdeclitech.entities.*;
import tn.example.backdeclitech.repositories.FeedBackRepository;
import tn.example.backdeclitech.repositories.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FeedbackService {

    private final FeedBackRepository feedBackRepository;
    private final ReservationRepository reservationRepository;
    private final SentimentService sentimentService;

    @Transactional
    public FeedbackResponse createFeedback(FeedbackRequest request, User parent) throws Exception {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new Exception("Réservation non trouvée"));

        if (!reservation.getParent().getId().equals(parent.getId())) {
            throw new Exception("Cette réservation ne vous appartient pas");
        }

        if (reservation.getStatus() != StatusReservation.COMPLETED) {
            throw new Exception("Vous ne pouvez donner un avis que pour une réservation complétée");
        }

        if (feedBackRepository.existsByReservationId(reservation.getId())) {
            throw new IllegalStateException("Vous avez déjà donné un avis pour cette réservation");
        }

        FeedBack feedback = new FeedBack();
        feedback.setContent(request.getContent());
        feedback.setRating(request.getRating());
        feedback.setSubmittedAt(LocalDateTime.now());
        feedback.setParent(parent);
        feedback.setModule(reservation.getModule());
        feedback.setReservation(reservation);

        tn.example.backdeclitech.entities.Sentiment sentimentEntity;
        try {
            SentimentService.Sentiment detected = sentimentService.analyzeSentiment(feedback.getContent());
            System.out.println("Sentiment détecté : " + detected);

            switch (detected) {
                case POSITIVE:
                    sentimentEntity = tn.example.backdeclitech.entities.Sentiment.POSITIVE;
                    break;
                case NEGATIVE:
                    sentimentEntity = tn.example.backdeclitech.entities.Sentiment.NEGATIVE;
                    break;
                case NEUTRAL:
                default:
                    sentimentEntity = tn.example.backdeclitech.entities.Sentiment.NEUTRAL;
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'analyse du sentiment : " + e.getMessage());
            sentimentEntity = determineSentimentByRating(feedback.getRating());
        }

        feedback.setSentiment(sentimentEntity);
        feedBackRepository.save(feedback);

        return mapToFeedbackResponse(feedback);
    }

    private tn.example.backdeclitech.entities.Sentiment determineSentimentByRating(int rating) {
        if (rating >= 4) {
            return tn.example.backdeclitech.entities.Sentiment.POSITIVE;
        } else if (rating <= 2) {
            return tn.example.backdeclitech.entities.Sentiment.NEGATIVE;
        } else {
            return tn.example.backdeclitech.entities.Sentiment.NEUTRAL;
        }
    }

    public boolean feedbackExists(Long reservationId, User parent) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new Exception("Réservation non trouvée"));

        if (!reservation.getParent().getId().equals(parent.getId())) {
            throw new Exception("Cette réservation ne vous appartient pas");
        }
        return feedBackRepository.existsByReservationId(reservationId);
    }

    private FeedbackResponse mapToFeedbackResponse(FeedBack feedback) {
        FeedbackResponse response = new FeedbackResponse();
        response.setId(feedback.getId());
        response.setContent(feedback.getContent());
        response.setRating(feedback.getRating());
        response.setSubmittedAt(feedback.getSubmittedAt());
        response.setSentiment(feedback.getSentiment());

        if (feedback.getParent() != null) {
            response.setParentName(feedback.getParent().getFirstName() + " " + feedback.getParent().getLastName());
            response.setParentId(feedback.getParent().getId());
            response.setParentPhone(feedback.getParent().getPhone());
        }

        if (feedback.getModule() != null) {
            response.setModuleName(feedback.getModule().getTitle());
            response.setModuleId(feedback.getModule().getId());
        }

        if (feedback.getReservation() != null) {
            response.setReservationId(feedback.getReservation().getId());

            if (feedback.getReservation().getChild() != null) {
                Child child = feedback.getReservation().getChild();
                response.setChildName(child.getFirstName() + " " + child.getLastName());
            }
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getAllFeedbacks() {
        return feedBackRepository.findAll()
                .stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedbackResponse markAsProcessed(int feedbackId) throws Exception {
        FeedBack feedback = feedBackRepository.findById(feedbackId)
                .orElseThrow(() -> new Exception("Avis non trouvé"));

        feedback.setProcessed(true);
        feedBackRepository.save(feedback);

        return mapToFeedbackResponse(feedback);
    }
}