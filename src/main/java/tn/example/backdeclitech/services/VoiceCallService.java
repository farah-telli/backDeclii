package tn.example.backdeclitech.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.example.backdeclitech.DTO.CallResponse;
import tn.example.backdeclitech.entities.Role;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.entities.VoiceCall;
import tn.example.backdeclitech.entities.VoiceCall.CallStatus;
import tn.example.backdeclitech.repositories.UserRepository;
import tn.example.backdeclitech.repositories.VoiceCallRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VoiceCallService {

    private final VoiceCallRepository voiceCallRepository;
    private final UserRepository userRepository;

    /**
     * Initier un appel vocal
     */
    public CallResponse initiateCall(Long callerId, Long receiverId, String sdpOffer) {
        log.debug("Initiating call from user {} to user {}", callerId, receiverId);

        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new IllegalArgumentException("Caller not found with ID: " + callerId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found with ID: " + receiverId));

        validateCallPermission(caller, receiver);

        if (!receiver.isActive()) {
            throw new IllegalStateException("Cannot call inactive user");
        }

        // Créer l'appel
        VoiceCall call = new VoiceCall();
        call.setCallId(UUID.randomUUID().toString());
        call.setCaller(caller);
        call.setReceiver(receiver);
        call.setStatus(CallStatus.INITIATED);
        call.setSdpOffer(sdpOffer);

        VoiceCall savedCall = voiceCallRepository.save(call);
        log.info("Call initiated: {}", savedCall.getCallId());

        return mapToResponse(savedCall);
    }

    /**
     * Répondre à un appel
     */
    public CallResponse answerCall(String callId, Long userId, String sdpAnswer) {
        log.debug("User {} answering call {}", userId, callId);

        VoiceCall call = voiceCallRepository.findByCallId(callId)
                .orElseThrow(() -> new IllegalArgumentException("Call not found with ID: " + callId));

        if (!call.getReceiver().getId().equals(userId)) {
            throw new AccessDeniedException("Only the receiver can answer this call");
        }

        if (call.getStatus() != CallStatus.INITIATED && call.getStatus() != CallStatus.RINGING) {
            throw new IllegalStateException("Call cannot be answered in current status: " + call.getStatus());
        }

        call.setStatus(CallStatus.ANSWERED);
        call.setAnsweredAt(LocalDateTime.now());
        call.setSdpAnswer(sdpAnswer);

        VoiceCall updatedCall = voiceCallRepository.save(call);
        log.info("Call answered: {}", callId);

        return mapToResponse(updatedCall);
    }

    /**
     * Rejeter un appel
     */
    public void rejectCall(String callId, Long userId) {
        log.debug("User {} rejecting call {}", userId, callId);

        VoiceCall call = voiceCallRepository.findByCallId(callId)
                .orElseThrow(() -> new IllegalArgumentException("Call not found with ID: " + callId));

        if (!call.getReceiver().getId().equals(userId)) {
            throw new AccessDeniedException("Only the receiver can reject this call");
        }

        if (call.getStatus() != CallStatus.INITIATED && call.getStatus() != CallStatus.RINGING) {
            throw new IllegalStateException("Call cannot be rejected in current status: " + call.getStatus());
        }

        call.setStatus(CallStatus.REJECTED);
        call.setEndedAt(LocalDateTime.now());

        voiceCallRepository.save(call);
        log.info("Call rejected: {}", callId);
    }

    /**
     * Terminer un appel
     */
    public void endCall(String callId, Long userId) {
        log.debug("User {} ending call {}", userId, callId);

        VoiceCall call = voiceCallRepository.findByCallId(callId)
                .orElseThrow(() -> new IllegalArgumentException("Call not found with ID: " + callId));

        if (!call.getCaller().getId().equals(userId) && !call.getReceiver().getId().equals(userId)) {
            throw new AccessDeniedException("Only call participants can end this call");
        }

        if (call.getStatus() != CallStatus.ANSWERED) {
            throw new IllegalStateException("Only active calls can be ended");
        }

        call.setStatus(CallStatus.ENDED);
        call.setEndedAt(LocalDateTime.now());

        // Calculer la durée
        if (call.getAnsweredAt() != null) {
            Duration duration = Duration.between(call.getAnsweredAt(), call.getEndedAt());
            call.setDuration((int) duration.getSeconds());
        }

        voiceCallRepository.save(call);
        log.info("Call ended: {}, duration: {} seconds", callId, call.getDuration());
    }

    /**
     * Annuler un appel (avant qu'il soit répondu)
     */
    public void cancelCall(String callId, Long userId) {
        log.debug("User {} cancelling call {}", userId, callId);

        VoiceCall call = voiceCallRepository.findByCallId(callId)
                .orElseThrow(() -> new IllegalArgumentException("Call not found with ID: " + callId));

        if (!call.getCaller().getId().equals(userId)) {
            throw new AccessDeniedException("Only the caller can cancel this call");
        }

        if (call.getStatus() != CallStatus.INITIATED && call.getStatus() != CallStatus.RINGING) {
            throw new IllegalStateException("Call cannot be cancelled in current status: " + call.getStatus());
        }

        call.setStatus(CallStatus.CANCELLED);
        call.setEndedAt(LocalDateTime.now());

        voiceCallRepository.save(call);
        log.info("Call cancelled: {}", callId);
    }

    /**
     * Obtenir l'historique des appels
     */
    @Transactional(readOnly = true)
    public List<CallResponse> getCallHistory(Long userId) {
        log.debug("Fetching call history for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        List<VoiceCall> calls = voiceCallRepository.findCallsByUser(userId);
        return calls.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les appels en attente
     */
    @Transactional(readOnly = true)
    public List<CallResponse> getPendingCalls(Long userId) {
        log.debug("Fetching pending calls for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        List<VoiceCall> calls = voiceCallRepository.findPendingCallsForUser(userId, CallStatus.INITIATED);
        return calls.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Valider les permissions d'appel
     */
    private void validateCallPermission(User caller, User receiver) {
        boolean isValid =
                (caller.getRole() == Role.PARENT && receiver.getRole() == Role.SUPERADMIN) ||
                        (caller.getRole() == Role.SUPERADMIN && receiver.getRole() == Role.PARENT);

        if (!isValid) {
            log.warn("Invalid call attempt between {} and {}", caller.getRole(), receiver.getRole());
            throw new AccessDeniedException(
                    String.format("Calls are only allowed between PARENT and SUPERADMIN roles. " +
                            "Attempted: %s <-> %s", caller.getRole(), receiver.getRole())
            );
        }
    }

    /**
     * Mapper VoiceCall vers CallResponse
     */
    private CallResponse mapToResponse(VoiceCall call) {
        CallResponse response = new CallResponse();
        response.setCallId(call.getCallId());
        response.setCallerId(call.getCaller().getId());
        response.setCallerName(call.getCaller().getFullName());
        response.setReceiverId(call.getReceiver().getId());
        response.setReceiverName(call.getReceiver().getFullName());
        response.setStatus(call.getStatus().name());
        response.setInitiatedAt(call.getInitiatedAt());
        response.setSdpOffer(call.getSdpOffer());
        response.setSdpAnswer(call.getSdpAnswer());
        return response;
    }
}