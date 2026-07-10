package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@Entity
@Table(name = "user_category")
@AllArgsConstructor
@NoArgsConstructor
public class UserCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private UUID id;

    @Column(name = "label", nullable = false, unique = true)
    private String label;

    @Column(name = "description")
    private String description;

    @Column(name = "access_level", nullable = false)
    @Builder.Default
    private Integer accessLevel = 1;

    @OneToMany(mappedBy = "category")
    private List<User> users;

    @ManyToMany
    @JoinTable(
            name = "category_permission",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<Permission> permissions;

}