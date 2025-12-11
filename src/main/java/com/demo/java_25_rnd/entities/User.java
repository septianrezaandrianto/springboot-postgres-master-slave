package com.demo.java_25_rnd.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(name="users", schema = "public")
@Entity
public class User {

    @Id
    @GeneratedValue(generator="system-uuid")
    @Column(name = "id", length = 100)
    private String id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
}
