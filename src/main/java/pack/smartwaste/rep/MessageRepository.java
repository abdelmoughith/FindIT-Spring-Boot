package pack.smartwaste.rep;

import org.springframework.data.jpa.repository.JpaRepository;
import pack.smartwaste.models.chat.Message;

import java.util.List;


public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderAndReceiver(String sender, String receiver);
}
