create sequence if not exists users_seq start with 1 increment by 50;
create table if not exists users
(
    id                      bigint          not null primary key,
    email                   varchar(255)    not null unique,
    password                varchar(255)    not null,
    wallet_address          varchar(255) unique,
    enabled                 boolean         not null,
    eth_balance             numeric(38, 10) not null,
    total_eth_profit_earned numeric(38, 10) not null,
    authority               varchar(255)    not null,
    created_at            timestamp(6)    not null,
    constraint authority_check CHECK ( authority in ('ROLE_ADMIN', 'ROLE_USER') )
);
create sequence if not exists bets_seq start with 1 increment by 50;
create table if not exists bets
(
    id                    bigint          not null primary key,
    admin_user_id         bigint          not null,
    title                 varchar(255)    not null,
    status                varchar(255)    not null,
    outcome               varchar(255)    not null,
    eth_win_stake_amount  numeric(38, 10) not null,
    eth_loss_stake_amount numeric(38, 10) not null,
    bets_for_win_count    bigint          not null,
    bets_for_loss_count   bigint          not null,
    eth_profit            numeric(38, 10) not null,
    eth_ticket_price      numeric(38, 10) not null,
    updated_by_admin_at   timestamp(6)    not null,
    created_at            timestamp(6)    not null,
    constraint outcome CHECK ( outcome in ('WIN', 'LOSE', 'NOT_DETERMINED')),
    constraint status CHECK ( status in ('CREATED', 'OPENED', 'CLOSED', 'RESOLVED'))
);
create table if not exists user_bet
(

    user_id bigint not null,
    bet_id  bigint not null,

    constraint fk_user_bet_bet_id foreign key (bet_id) references bets (id),
    constraint fk_user_bet_user_id foreign key (user_id) references users (id),
    constraint pk_user_bet primary key (user_id, bet_id)
);
create sequence if not exists crypto_transactions_seq start with 1 increment by 50;
create table if not exists crypto_transactions
(
    id            bigint       not null primary key,
    hash          varchar(255) unique,
    user_id       bigint       not null,
    type          varchar(255) not null,
    state         varchar(255) not null,
    eth_amount    numeric(38, 8),
    confirmations bigint       not null,
    created_at    timestamp(6) not null,
    constraint state_check CHECK ( state in ('PROCESSING', 'FAILING', 'PROCESSED') ),
    constraint type_check CHECK ( type in ('DEPOSIT', 'WITHDRAW')),
    constraint fk_user_id foreign key (user_id) references users (id)
);
create sequence if not exists bet_transactions_seq start with 1 increment by 50;
create table if not exists bet_transactions
(
    id                  bigint          not null primary key,
    user_id             bigint          not null,
    bet_id              bigint          not null,
    eth_amount          numeric(38, 10) not null,
    eth_profit          numeric(38, 10) not null,
    predicted_outcome   varchar(255)    not null,
    transaction_outcome varchar(255)    not null,
    created_at          timestamp(6)    not null,
    constraint predicted_outcome CHECK ( predicted_outcome in ('WIN', 'LOSE', 'NOT_DETERMINED')),
    constraint transaction_outcome CHECK ( transaction_outcome in ('WIN', 'LOSE', 'NOT_DETERMINED') ),
    constraint fk_customer_bets_user_id foreign key (user_id) references users (id),
    constraint fk_customer_bets_bet_id foreign key (bet_id) references bets (id),
    constraint uq_user_transaction_bets unique (user_id, bet_id)
);

insert into users (id, email, password, wallet_address, enabled, eth_balance, total_eth_profit_earned, authority,
                   created_at)
values (nextval('users_seq'), 'nimofy1997@gmail.com', 'IloveMyWifeCrazyCat',
        '0xb5de66ff2864d6ecb2793c68d417c2586eafcdd7', true, 0.00, 0.00, 'ROLE_ADMIN', now());