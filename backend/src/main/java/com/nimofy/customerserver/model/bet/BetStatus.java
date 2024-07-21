package com.nimofy.customerserver.model.bet;

public enum BetStatus {
    CREATED, // Game is created
    OPENED, // Game is open for followers to place their bets
    CLOSED, // Game is closed and followers can not place their bets
    RESOLVED // Game is resolved and profit is calculated for each player and payout is given to each player
}