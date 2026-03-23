package com.mktplace.model;

import com.mktplace.enums.DocumentType;
import com.mktplace.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_city_state", columnList = "city,state"),
        @Index(name = "idx_users_lat_lng", columnList = "latitude,longitude")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, unique = true, length = 18)
    private String documentNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DocumentType documentType;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column(length = 10)
    private String postalCode;
    private String street;
    private String streetNumber;
    private String complement;
    private String neighborhood;
    private String city;
    @Column(length = 2)
    private String state;
    private String companyName;
    private Double latitude;
    private Double longitude;
    @Column(nullable = false)
    private Instant createdAt;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_name")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
