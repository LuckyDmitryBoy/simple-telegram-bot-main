package io.proj3ct.SpringDemoBot.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name="adsTable")
@Setter
@Getter
public class Ads {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private String ad;
}
