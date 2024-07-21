package com.nimofy.customerserver.model.user;

import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.transaction.bet.BetTransaction;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Data
@Builder
@ToString(exclude = {"betTransactions", "bets"})
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Builder.Default
    @Column(nullable = false)
    private String password = UUID.randomUUID().toString();
    @Column(unique = true, name = "wallet_address")
    private String walletAddress;
    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = false;
    @Builder.Default
    @Column(nullable = false, name = "eth_balance")
    private BigDecimal ethBalance = BigDecimal.ZERO;
    @Builder.Default
    @Column(nullable = false, name = "total_eth_profit_earned")
    private BigDecimal totalEthProfitEarned = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Authority authority;

    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_bet",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "bet_id")
    )
    private Set<Bet> bets = new HashSet<>();

    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<BetTransaction> betTransactions = new ArrayList<>();
    @Builder.Default
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.getId());
    }
}