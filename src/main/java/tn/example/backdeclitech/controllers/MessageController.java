package tn.example.backdeclitech.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.example.backdeclitech.DTO.*;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.UserRepository;
import tn.example.backdeclitech.services.FileStorageService;
import tn.example.backdeclitech.services.MessageService;
import tn.example.backdeclitech.services.VoiceCallService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageService messageService;
    private final VoiceCallService voiceCallService;
    private final FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user;
        } else if (principal instanceof String username) {
            if (username.equals("anonymousUser")) {
                throw new IllegalStateException("Anonymous user not allowed");
            }
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found for username: " + username));
        } else {
            throw new IllegalStateException("Invalid principal type");
        }
    }


    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "MessageController is working!"
        ));
    }

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody MessageRequest request) {
        User currentUser = getCurrentUser();
        log.info("User {} sending message to user {}", currentUser.getId(), request.getReceiverId());

        MessageResponse response = messageService.sendMessage(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<MessageResponse>> getConversation(@PathVariable Long otherUserId) {
        User currentUser = getCurrentUser();
        log.info("User {} fetching conversation with user {}", currentUser.getId(), otherUserId);

        List<MessageResponse> messages = messageService.getConversation(currentUser.getId(), otherUserId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<MessageResponse>> getUnreadMessages() {
        User currentUser = getCurrentUser();
        log.info("User {} fetching unread messages", currentUser.getId());

        List<MessageResponse> messages = messageService.getUnreadMessages(currentUser.getId());
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> countUnreadMessages() {
        User currentUser = getCurrentUser();

        Long count = messageService.countUnreadMessages(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long messageId) {
        User currentUser = getCurrentUser();
        log.info("User {} marking message {} as read", currentUser.getId(), messageId);

        messageService.markAsRead(messageId, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Message marked as read"));
    }

    @PutMapping("/conversation/{otherUserId}/read")
    public ResponseEntity<Map<String, String>> markConversationAsRead(@PathVariable Long otherUserId) {
        User currentUser = getCurrentUser();
        log.info("User {} marking conversation with {} as read", currentUser.getId(), otherUserId);

        // Cette fonctionnalité est gérée automatiquement par getConversation
        messageService.getConversation(currentUser.getId(), otherUserId);
        return ResponseEntity.ok(Map.of("message", "Conversation marked as read"));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        User currentUser = getCurrentUser();
        log.info("User {} deleting message {}", currentUser.getId(), messageId);

        messageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/partners")
    public ResponseEntity<List<ConversationPartnerDTO>> getConversationPartners() {
        User currentUser = getCurrentUser();
        log.info("User {} fetching conversation partners", currentUser.getId());

        List<ConversationPartnerDTO> partners = messageService.getConversationPartners(currentUser.getId());
        return ResponseEntity.ok(partners);
    }

    // ========== ENDPOINTS MESSAGES VOCAUX ==========

    @PostMapping(value = "/send/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> sendVoiceMessage(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("receiverId") Long receiverId,
            @RequestParam(value = "duration", required = false) Integer duration) {

        User currentUser = getCurrentUser();
        log.info("User {} sending voice message to user {}", currentUser.getId(), receiverId);

        if (audioFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        MessageResponse response = messageService.sendVoiceMessage(
                currentUser.getId(),
                receiverId,
                audioFile,
                duration
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/voice/{fileName:.+}")
    public ResponseEntity<Resource> getVoiceMessage(@PathVariable String fileName) {
        User currentUser = getCurrentUser();
        log.info("User {} fetching voice message: {}", currentUser.getId(), fileName);

        Resource resource = fileStorageService.loadVoiceMessage(fileName);

        String contentType = "audio/webm";
        if (fileName.endsWith(".mp3")) {
            contentType = "audio/mpeg";
        } else if (fileName.endsWith(".wav")) {
            contentType = "audio/wav";
        } else if (fileName.endsWith(".ogg")) {
            contentType = "audio/ogg";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }


    @PostMapping("/call/initiate")
    public ResponseEntity<CallResponse> initiateCall(@Valid @RequestBody CallInitiateRequest request) {
        User currentUser = getCurrentUser();
        log.info("User {} initiating call to user {}", currentUser.getId(), request.getReceiverId());

        CallResponse response = voiceCallService.initiateCall(
                currentUser.getId(),
                request.getReceiverId(),
                request.getSdpOffer()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/call/answer")
    public ResponseEntity<CallResponse> answerCall(@Valid @RequestBody CallAnswerRequest request) {
        User currentUser = getCurrentUser();
        log.info("User {} answering call {}", currentUser.getId(), request.getCallId());

        CallResponse response = voiceCallService.answerCall(
                request.getCallId(),
                currentUser.getId(),
                request.getSdpAnswer()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/call/reject")
    public ResponseEntity<Map<String, String>> rejectCall(@Valid @RequestBody CallEndRequest request) {
        User currentUser = getCurrentUser();
        log.info("User {} rejecting call {}", currentUser.getId(), request.getCallId());

        voiceCallService.rejectCall(request.getCallId(), currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Call rejected"));
    }

    @PostMapping("/call/end")
    public ResponseEntity<Map<String, String>> endCall(@Valid @RequestBody CallEndRequest request) {
        User currentUser = getCurrentUser();
        log.info("User {} ending call {}", currentUser.getId(), request.getCallId());

        voiceCallService.endCall(request.getCallId(), currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Call ended"));
    }

    @PostMapping("/call/cancel")
    public ResponseEntity<Map<String, String>> cancelCall(@Valid @RequestBody CallEndRequest request) {
        User currentUser = getCurrentUser();
        log.info("User {} cancelling call {}", currentUser.getId(), request.getCallId());

        voiceCallService.cancelCall(request.getCallId(), currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Call cancelled"));
    }

    @GetMapping("/call/history")
    public ResponseEntity<List<CallResponse>> getCallHistory() {
        User currentUser = getCurrentUser();
        log.info("User {} fetching call history", currentUser.getId());

        List<CallResponse> calls = voiceCallService.getCallHistory(currentUser.getId());
        return ResponseEntity.ok(calls);
    }

    @GetMapping("/call/pending")
    public ResponseEntity<List<CallResponse>> getPendingCalls() {
        User currentUser = getCurrentUser();
        log.info("User {} fetching pending calls", currentUser.getId());

        List<CallResponse> calls = voiceCallService.getPendingCalls(currentUser.getId());
        return ResponseEntity.ok(calls);
    }
    // Ajouter ces nouveaux endpoints dans MessageController

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody MessageRequest request) {
        User currentUser = getCurrentUser();
        log.info("User {} updating message {}", currentUser.getId(), messageId);

        MessageResponse response = messageService.updateMessage(messageId, currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{messageId}/permanent")
    public ResponseEntity<Void> deleteMessagePermanently(@PathVariable Long messageId) {
        User currentUser = getCurrentUser();
        log.info("User {} permanently deleting message {}", currentUser.getId(), messageId);

        messageService.deleteMessagePermanently(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}