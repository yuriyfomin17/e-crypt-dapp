package com.nimofy.customerserver.model.transaction.crypto;

import com.nimofy.customerserver.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@Table(name = "crypto_transactions")
@ToString(exclude = "user")
@AllArgsConstructor
@NoArgsConstructor
public class CryptoTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "hash", unique = true)
    private String hash;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;
    @Column(name = "eth_amount")
    private BigDecimal ethAmount;
    @Builder.Default
    @Column(nullable = false)
    private Long confirmations = 0L;
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}