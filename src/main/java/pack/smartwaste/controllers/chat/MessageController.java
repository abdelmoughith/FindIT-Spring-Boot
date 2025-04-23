package pack.smartwaste.controllers.chat;

import org.springframework.web.bind.annotation.*;
import pack.smartwaste.models.chat.Message;
import pack.smartwaste.rep.MessageRepository;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/{sender}/{receiver}")
    public List<Message> getChatHistory(@PathVariable String sender, @PathVariable String receiver) {
        return messageRepository.findBySenderAndReceiver(sender, receiver);
    }
}

