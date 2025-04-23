package pack.smartwaste.controllers.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import pack.smartwaste.models.chat.Message;
import pack.smartwaste.models.chat.ChatMessage;
import pack.smartwaste.rep.MessageRepository;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @MessageMapping("/chat")
    public void sendMessage(ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());

        // Save to DB
        Message message = new Message();
        message.setSender(chatMessage.getSender());
        message.setReceiver(chatMessage.getReceiver());
        message.setContent(chatMessage.getContent());
        message.setTimestamp(chatMessage.getTimestamp());
        try {
            messageRepository.save(message);
        } catch (Exception e) {
            System.err.println("Error saving message: " + e.getMessage());
        }


        // Send to receiver (user-specific)
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver(),
                "/queue/messages",
                chatMessage
        );

        // Send confirmation to sender (optional)
        /*
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSender(),
                "/queue/messages",
                chatMessage
        );

         */

        System.out.println("Message processed: " + chatMessage);
    }

}



