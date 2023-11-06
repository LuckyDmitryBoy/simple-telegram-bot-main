package io.proj3ct.SpringDemoBot.model;

import lombok.Data;

@Data
public class WeartherModel {
    private String name;
    private Double temp;
    private Double humidity;
    private String icon;
    private String main;
}
