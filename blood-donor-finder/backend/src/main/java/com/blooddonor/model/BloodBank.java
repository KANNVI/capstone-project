package com.blooddonor.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
public class BloodBank {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String name;
    public String city;
    public String phone;
    @ElementCollection(fetch = FetchType.EAGER) public Map<String, Integer> stock = new HashMap<>();
}
