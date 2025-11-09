package tn.example.backdeclitech.services;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.example.backdeclitech.DTO.ReservationRequest;
import tn.example.backdeclitech.DTO.ReservationResponse;
import tn.example.backdeclitech.entities.Module; // <-- Import explicit ici
import tn.example.backdeclitech.entities.ModuleSession;
import tn.example.backdeclitech.entities.Reservation;
import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.entities.StatusReservation;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.ModuleRepository;
import tn.example.backdeclitech.repositories.ModuleSessionRepository;
import tn.example.backdeclitech.repositories.ReservationRepository;
import tn.example.backdeclitech.repositories.ChildRepository;

import java.time.*;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationService {

    public class ReservationCreatedEvent extends ApplicationEvent {
        private final Reservation reservation;

        public ReservationCreatedEvent(Reservation reservation) {
            super(reservation);
            this.reservation = reservation;
        }

        public Reservation getReservation() {
            return reservation;
        }
    }

    public class ReservationCanceledEvent {
        private final Reservation reservation;

        public ReservationCanceledEvent(Reservation reservation) {
            this.reservation = reservation;
        }

        public Reservation getReservation() {
            return reservation;
        }
    }

    private final ReservationRepository reservationRepository;
    private final ModuleSessionRepository moduleSessionRepository;
    private final ChildRepository childRepository;
    private final ModuleRepository moduleRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, User parent) throws Exception {
        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new Exception("Enfant non trouvé"));
        if (!child.getParent().getId().equals(parent.getId())) {
            throw new Exception("Cet enfant ne vous appartient pas.");
        }
        ModuleSession session = moduleSessionRepository.findById(request.getModuleSessionId())
                .orElseThrow(() -> new Exception("Session de module non trouvée."));
        Module module = session.getModule();
        if (module == null) {
            throw new Exception("Aucun module associé à cette session.");
        }

        LocalDateTime sessionDateTime = session.getDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime()
                .withHour(session.getStartTime().getHour())
                .withMinute(session.getStartTime().getMinute());

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(sessionDateTime)) {
            throw new Exception("Impossible de réserver une session qui a déjà commencé ou qui est terminée.");
        }

        if (reservationRepository.hasReservationOnSameDay(child.getId(), session.getDate())) {
            throw new Exception("Cet enfant a déjà une réservation à cette date. Limite d'une réservation par jour.");
        }

        if (child.getAge() < session.getTrancheAgeMin() || child.getAge() > session.getTrancheAgeMax()) {
            throw new Exception("L'enfant n'est pas dans la tranche d'âge requise pour cette session.");
        }

        if (session.getEnrolledCount() >= session.getCapcity()) {
            throw new Exception("Cette session est complète.");
        }

        if (reservationRepository.existsByChildIdAndSessionId(child.getId(), session.getId())) {
            throw new Exception("Cet enfant est déjà inscrit à cette session.");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(session.getDate());
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);

        long reservationsThisWeek = reservationRepository.countReservationsForChildInWeek(child.getId(), weekOfYear, year);
        if (reservationsThisWeek >= 2) {
            throw new Exception("L'enfant a déjà atteint la limite de 2 réservations cette semaine.");
        }

        Reservation reservation = new Reservation();
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatus(StatusReservation.RESERVED);
        reservation.setSession(session);
        reservation.setChild(child);
        reservation.setParent(parent);
        reservation.setModule(module);

        reservationRepository.save(reservation);
        eventPublisher.publishEvent(new ReservationCreatedEvent(reservation));

        session.setEnrolledCount(session.getEnrolledCount() + 1);
        session.setCapcity(session.getCapcity() - 1);
        moduleSessionRepository.save(session);

        module.setEnrolledCount(module.getEnrolledCount() + 1);
        moduleRepository.save(module);

        return mapToReservationResponse(reservation);
    }


    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByParent(User parent) {
        List<Reservation> reservations = reservationRepository.findByParentId(parent.getId());
        return mapToReservationResponseList(reservations);
    }


    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByChild(Long childId, User parent) throws Exception {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new Exception("Enfant non trouvé"));
        if (!child.getParent().getId().equals(parent.getId())) {
            throw new Exception("Cet enfant ne vous appartient pas.");
        }
        List<Reservation> reservations = reservationRepository.findByChildId(childId);
        return mapToReservationResponseList(reservations);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByModule(Long moduleId, User parent) {
        List<Reservation> reservations = reservationRepository.findByModuleIdAndParentId(moduleId, parent.getId());
        return mapToReservationResponseList(reservations);
    }


    @Transactional
    public void cancelReservation(Long reservationId, User parent) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new Exception("Réservation non trouvée."));
        if (!reservation.getParent().getId().equals(parent.getId())) {
            throw new Exception("Cette réservation ne vous appartient pas.");
        }

        LocalDateTime sessionDateTime = reservation.getSession().getDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime()
                .withHour(reservation.getSession().getStartTime().getHour())
                .withMinute(reservation.getSession().getStartTime().getMinute());

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, sessionDateTime);
        if (duration.toHours() < 5) {
            throw new Exception("Vous ne pouvez pas annuler une réservation moins de 5 heures avant le début de la session.");
        }

        reservation.setStatus(StatusReservation.CANCELED);
        reservationRepository.save(reservation);
        eventPublisher.publishEvent(new ReservationCanceledEvent(reservation));

        ModuleSession session = reservation.getSession();
        session.setEnrolledCount(session.getEnrolledCount() - 1);
        session.setCapcity(session.getCapcity() + 1);
        moduleSessionRepository.save(session);

        Module module = reservation.getModule();
        module.setEnrolledCount(module.getEnrolledCount() - 1);
        moduleRepository.save(module);
    }


    @Transactional
    public void completeReservation(Long reservationId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new Exception("Réservation non trouvée."));
        if (reservation.getStatus() != StatusReservation.RESERVED) {
            throw new Exception("Seules les réservations au statut RESERVED peuvent être complétées.");
        }

        ModuleSession session = reservation.getSession();
        LocalDateTime sessionEndDateTime = session.getDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime()
                .withHour(session.getEndTime().getHour())
                .withMinute(session.getEndTime().getMinute());


        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(sessionEndDateTime)) {
            throw new Exception("La session n'est pas encore terminée.");
        }

        reservation.setStatus(StatusReservation.COMPLETED);
        reservationRepository.save(reservation);
    }

    @Transactional
    public void updateCompletedReservations() {
        List<Reservation> reservations = reservationRepository.findByStatus(StatusReservation.RESERVED);
        LocalDateTime now = LocalDateTime.now();

        for (Reservation reservation : reservations) {
            try {
                ModuleSession session = reservation.getSession();
                if (session != null && session.getEndTime() != null && session.getDate() != null) {
                    LocalDateTime sessionEndDateTime = session.getDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime()
                            .withHour(session.getEndTime().getHour())
                            .withMinute(session.getEndTime().getMinute());

                    if (now.isAfter(sessionEndDateTime)) {
                        reservation.setStatus(StatusReservation.COMPLETED);
                        reservationRepository.save(reservation);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour de la réservation " + reservation.getId() + ": " + e.getMessage());
            }
        }
    }


    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return mapToReservationResponseList(reservations);
    }

    private List<ReservationResponse> mapToReservationResponseList(List<Reservation> reservations) {
        return reservations.stream()
                .map(this::mapToReservationResponse)
                .collect(Collectors.toList());
    }

    private ReservationResponse mapToReservationResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setDateReservation(reservation.getDateReservation());
        response.setStatus(reservation.getStatus());

        if (reservation.getChild() != null) {
            response.setChildId(reservation.getChild().getId());
            response.setChildName(reservation.getChild().getFirstName() + " " + reservation.getChild().getLastName());
        }

        if (reservation.getParent() != null) {
            response.setParentName(reservation.getParent().getFirstName() + " " + reservation.getParent().getLastName());
            response.setPhone(reservation.getParent().getPhone());
        }

        if (reservation.getModule() != null) {
            response.setModuleId(reservation.getModule().getId());
            response.setModuleName(reservation.getModule().getTitle());
        }

        if (reservation.getSession() != null) {
            response.setModuleSessionId(reservation.getSession().getId());
            response.setSessionDate(reservation.getSession().getDate());
            response.setSessionTime(reservation.getSession().getStartTime() + " - " + reservation.getSession().getEndTime());

            if (reservation.getSession().getCoBuildSpace() != null) {
                response.setCobuildSpaceName(reservation.getSession().getCoBuildSpace().getName());
            }
        }

        return response;
    }
}
