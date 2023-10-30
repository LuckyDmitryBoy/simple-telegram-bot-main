package io.proj3ct.SpringDemoBot.model;





import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
@Data
public class Joke {
    @Column(length = 2550000)
    private String body;
    private String category;
    @Id

    private Integer id;

    private double rating;
}
