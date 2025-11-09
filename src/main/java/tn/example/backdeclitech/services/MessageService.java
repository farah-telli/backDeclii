package tn.example.backdeclitech.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import tn.example.backdeclitech.DTO.ConversationPartnerDTO;
import tn.example.backdeclitech.DTO.MessageRequest;
import tn.example.backdeclitech.DTO.MessageResponse;
import tn.example.backdeclitech.entities.Message;
import tn.example.backdeclitech.repositories.MessageRepository;
import tn.example.backdeclitech.entities.Role;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.base-url:http://localhost:8089}")
    private String baseUrl;


    public MessageResponse sendMessage(Long senderId, MessageRequest request) {
        log.debug("Sending message from user {} to user {}", senderId, request.getReceiverId());

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found with ID: " + senderId));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found with ID: " + request.getReceiverId()));

        validateChatPermission(sender, receiver);

        if (!receiver.isActive()) {
            throw new IllegalStateException("Cannot send message to inactive user");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.getContent());
        message.setMessageType(Message.MessageType.TEXT);

        Message savedMessage = messageRepository.save(message);
        log.info("Message sent successfully: ID {}", savedMessage.getId());

        return mapToResponse(savedMessage);
    }

    /**
     * Envoyer un message vocal
     */
    public MessageResponse sendVoiceMessage(Long senderId, Long receiverId,
                                            MultipartFile audioFile, Integer duration) {
        log.debug("Sending voice message from user {} to user {}", senderId, receiverId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found with ID: " + senderId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found with ID: " + receiverId));

        validateChatPermission(sender, receiver);

        if (!receiver.isActive()) {
            throw new IllegalStateException("Cannot send message to inactive user");
        }

        // Sauvegarder le fichier audio
        String fileName = fileStorageService.storeVoiceMessage(audioFile, senderId, receiverId);

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent("🎤 Message vocal");
        message.setMessageType(Message.MessageType.VOICE);
        message.setAudioFilePath(fileName);
        message.setAudioDuration(duration);

        Message savedMessage = messageRepository.save(message);
        log.info("Voice message sent successfully: ID {}", savedMessage.getId());

        return mapToResponse(savedMessage);
    }

    /**
     * Obtenir la conversation entre deux utilisateurs
     */
    @Transactional(readOnly = false)
    public List<MessageResponse> getConversation(Long currentUserId, Long otherUserId) {
        log.debug("Fetching conversation between user {} and user {}", currentUserId, otherUserId);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + otherUserId));

        validateChatPermission(currentUser, otherUser);

        List<Message> messages = messageRepository.findConversation(currentUserId, otherUserId);

        long markedCount = messages.stream()
                .filter(m -> m.getReceiver().getId().equals(currentUserId) && !m.getIsRead())
                .peek(m -> m.setIsRead(true))
                .count();

        if (markedCount > 0) {
            log.debug("Marked {} messages as read", markedCount);
        }

        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les messages non lus
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getUnreadMessages(Long userId) {
        log.debug("Fetching unread messages for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        List<Message> messages = messageRepository.findUnreadMessages(userId);
        log.debug("Found {} unread messages", messages.size());

        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Compter les messages non lus
     */
    @Transactional(readOnly = true)
    public Long countUnreadMessages(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        Long count = messageRepository.countUnreadMessages(userId);
        log.debug("User {} has {} unread messages", userId, count);
        return count;
    }

    /**
     * Marquer un message comme lu
     */
    public void markAsRead(Long messageId, Long currentUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        if (!message.getReceiver().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only mark your own received messages as read");
        }

        if (!message.getIsRead()) {
            message.setIsRead(true);
            messageRepository.save(message);
            log.debug("Message {} marked as read by user {}", messageId, currentUserId);
        }
    }

    /**
     * Supprimer un message
     */
    public void deleteMessage(Long messageId, Long currentUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        if (!message.getSender().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own sent messages");
        }

        if (!message.getIsDeleted()) {
            message.setIsDeleted(true);

            // Supprimer le fichier audio si c'est un message vocal
            if (message.getMessageType() == Message.MessageType.VOICE &&
                    message.getAudioFilePath() != null) {
                fileStorageService.deleteVoiceMessage(message.getAudioFilePath());
            }

            messageRepository.save(message);
            log.info("Message {} soft-deleted by user {}", messageId, currentUserId);
        }
    }

    /**
     * Obtenir les partenaires de conversation
     */
    @Transactional(readOnly = true)
    public List<ConversationPartnerDTO> getConversationPartners(Long userId) {
        log.debug("Fetching conversation partners for user {}", userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<User> partners = getEligibleChatPartners(currentUser);

        List<ConversationPartnerDTO> result = new ArrayList<>();
        for (User partner : partners) {
            List<Message> conversation = messageRepository.findConversation(userId, partner.getId());

            long unreadCount = conversation.stream()
                    .filter(m -> m.getReceiver().getId().equals(userId) && !m.getIsRead())
                    .count();

            LocalDateTime lastMessageTime = conversation.isEmpty() ? null
                    : conversation.get(conversation.size() - 1).getSentAt();

            result.add(new ConversationPartnerDTO(
                    partner.getId(),
                    partner.getFullName(),
                    partner.getRole().name(),
                    unreadCount,
                    lastMessageTime
            ));
        }

        result.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        log.debug("Found {} conversation partners", result.size());
        return result;
    }

    /**
     * Obtenir les partenaires de chat éligibles
     */
    private List<User> getEligibleChatPartners(User currentUser) {
        if (currentUser.getRole() == Role.PARENT) {
            return userRepository.findByRole(Role.SUPERADMIN).stream()
                    .filter(User::isActive)
                    .collect(Collectors.toList());
        } else if (currentUser.getRole() == Role.SUPERADMIN) {
            return userRepository.findByRole(Role.PARENT).stream()
                    .filter(User::isActive)
                    .collect(Collectors.toList());
        } else {
            throw new AccessDeniedException("Only PARENT and SUPERADMIN roles can use chat. Current role: " + currentUser.getRole());
        }
    }

    /**
     * Valider les permissions de chat
     */
    private void validateChatPermission(User user1, User user2) {
        boolean isValid =
                (user1.getRole() == Role.PARENT && user2.getRole() == Role.SUPERADMIN) ||
                        (user1.getRole() == Role.SUPERADMIN && user2.getRole() == Role.PARENT);

        if (!isValid) {
            log.warn("Invalid chat attempt between {} and {}", user1.getRole(), user2.getRole());
            throw new AccessDeniedException(
                    String.format("Chat is only allowed between PARENT and SUPERADMIN roles. " +
                            "Attempted: %s <-> %s", user1.getRole(), user2.getRole())
            );
        }
    }

    private MessageResponse mapToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getFullName());
        response.setReceiverId(message.getReceiver().getId());
        response.setReceiverName(message.getReceiver().getFullName());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setIsRead(message.getIsRead());
        response.setMessageType(message.getMessageType().name());
        response.setIsEdited(message.getIsEdited()); // NOUVEAU
        response.setEditedAt(message.getEditedAt()); // NOUVEAU

        if (message.getMessageType() == Message.MessageType.VOICE &&
                message.getAudioFilePath() != null) {
            response.setAudioUrl(baseUrl + "/api/chat/voice/" + message.getAudioFilePath());
            response.setAudioDuration(message.getAudioDuration());
        }

        return response;
    }

    public MessageResponse updateMessage(Long messageId, Long currentUserId, MessageRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        if (!message.getSender().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only edit your own sent messages");
        }

        if (message.getIsDeleted()) {
            throw new IllegalStateException("Cannot edit a deleted message");
        }

        if (message.getMessageType() == Message.MessageType.VOICE) {
            throw new IllegalStateException("Cannot edit voice messages");
        }

        // Vérifier que le message n'est pas trop ancien (optionnel: ex. 15 minutes)
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        if (message.getSentAt().isBefore(fifteenMinutesAgo)) {
            throw new IllegalStateException("Cannot edit messages older than 15 minutes");
        }

        message.setContent(request.getContent());
        message.setIsEdited(true);
        message.setEditedAt(LocalDateTime.now());

        Message updatedMessage = messageRepository.save(message);
        log.info("Message {} updated by user {}", messageId, currentUserId);

        return mapToResponse(updatedMessage);
    }


    public void deleteMessagePermanently(Long messageId, Long currentUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        if (!message.getSender().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own sent messages");
        }

        if (message.getMessageType() == Message.MessageType.VOICE &&
                message.getAudioFilePath() != null) {
            fileStorageService.deleteVoiceMessage(message.getAudioFilePath());
        }

        messageRepository.delete(message);
        log.info("Message {} permanently deleted by user {}", messageId, currentUserId);
    }
}