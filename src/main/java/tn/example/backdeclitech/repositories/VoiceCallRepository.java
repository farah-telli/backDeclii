package tn.example.backdeclitech.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.example.backdeclitech.entities.VoiceCall;
import tn.example.backdeclitech.entities.VoiceCall.CallStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceCallRepository extends JpaRepository<VoiceCall, Long> {

    Optional<VoiceCall> findByCallId(String callId);

    @Query("SELECT c FROM VoiceCall c WHERE " +
            "(c.caller.id = :userId OR c.receiver.id = :userId) " +
            "ORDER BY c.initiatedAt DESC")
    List<VoiceCall> findCallsByUser(@Param("userId") Long userId);

    @Query("SELECT c FROM VoiceCall c WHERE " +
            "c.receiver.id = :userId AND c.status = :status " +
            "ORDER BY c.initiatedAt DESC")
    List<VoiceCall> findPendingCallsForUser(@Param("userId") Long userId,
                                            @Param("status") CallStatus status);

    @Query("SELECT c FROM VoiceCall c WHERE " +
            "((c.caller.id = :userId1 AND c.receiver.id = :userId2) OR " +
            "(c.caller.id = :userId2 AND c.receiver.id = :userId1)) " +
            "ORDER BY c.initiatedAt DESC")
    List<VoiceCall> findCallsBetweenUsers(@Param("userId1") Long userId1,
                                          @Param("userId2") Long userId2);
}