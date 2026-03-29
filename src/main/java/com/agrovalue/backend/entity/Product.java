package com.agrovalue.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private int quantity;

    private String imageUrl;

    // Many products → one farmer (user)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}