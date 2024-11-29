package com.rsmanager.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "account_bank")
    private String accountBank;

    @Column(name = "account_holder")
    private String accountHolder;

    @Column(name = "account_currency")
    private String accountCurrency;

    @Column(name = "account_currency_code")
    private String accountCurrencyCode;

    @Column(name = "account_region")
    private String accountRegion;

    @Column(name = "account_status", nullable = false)
    private Boolean accountStatus;

    @Column(name = "account_comments")
    private String accountComments;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }
    
}
