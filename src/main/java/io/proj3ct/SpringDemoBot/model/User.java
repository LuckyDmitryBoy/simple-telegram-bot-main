package io.proj3ct.SpringDemoBot.model;





import lombok.Data;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "usersTable")
@Data
public class User {
    @Id
    private Long chatId;
    private String firstName;
    private String lastName;
    private String userName;
    private String bio;
    private String description;
    private String pinnedMessage;
    private Timestamp registeredAt;
}
