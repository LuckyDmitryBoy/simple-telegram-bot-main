package io.proj3ct.SpringDemoBot.model;





import lombok.Data;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "usersDataTable")
@Data
public class User {


    @Id
    private Long chatId;
    private Boolean embedeJoke;
    private String phoneNumber;
    private Timestamp registeredAt;

    private String firstName;

    private String lastName;

    private String userName;
    private Double latitude;
    private Double longitude;
    private String bio;
    private String description;
    private String pinnedMessage;
}
