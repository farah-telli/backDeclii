package tn.example.backdeclitech.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallResponse {


        private String callId;
        private Long callerId;
        private String callerName;
        private Long receiverId;
        private String receiverName;
        private String status;
        private LocalDateTime initiatedAt;
        private String sdpOffer;
        private String sdpAnswer;
    }