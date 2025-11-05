package com.ktb.community.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "oauth_users")
@Getter
@Setter
public class OauthUser extends Timestamped {

    @Id
    @Column(name = "oauth_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String provider;

    @NotNull
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
