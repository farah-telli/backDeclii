package tn.example.backdeclitech.services;

import tn.example.backdeclitech.DTO.ApiResponse;
import tn.example.backdeclitech.DTO.ReservationRequest;
import tn.example.backdeclitech.DTO.ReservationResponse;

import java.util.List;

public interface IReservationService {

    ApiResponse createReservation(ReservationRequest request, Long parentId);


    ApiResponse cancelReservation(Long reservationId, Long parentId);

    List<ReservationResponse> getParentReservations(Long parentId);


    List<ReservationResponse> getChildReservations(Long childId, Long parentId);


    boolean hasReachedWeeklyLimit(Long childId);
}