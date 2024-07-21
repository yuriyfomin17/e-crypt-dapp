package com.nimofy.customerserver.model.transaction.bet;

import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.Outcome;
import com.nimofy.customerserver.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString(exclude = {"bet", "user"})
@Entity
@Table(name = "bet_transactions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bet_id")
    private Bet bet;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Builder.Default
    @Column(nullable = false, name = "eth_amount")
    private BigDecimal ethAmount = BigDecimal.ZERO;
    @Builder.Default
    @Column(nullable = false, name = "eth_profit")
    private BigDecimal ethProfit = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "predicted_outcome")
    private Outcome predictedOutcome;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "transaction_outcome")
    private Outcome transactionOutcome = Outcome.NOT_DETERMINED;
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BetTransaction betTransaction)) return false;
        return id != null && id.equals(betTransaction.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}