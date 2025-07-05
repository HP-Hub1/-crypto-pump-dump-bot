package model;


import lombok.Data;

@Data
public class UserSettings {
    private String pair = "BTCUSDT";
    private int intervalSeconds = 300;
    private double pumpThresholdPercent = 1.5;
}
