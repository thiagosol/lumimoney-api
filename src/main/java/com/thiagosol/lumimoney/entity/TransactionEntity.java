package com.thiagosol.lumimoney.entity;

import com.thiagosol.lumimoney.entity.enums.TransactionType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionEntity extends PanacheEntity {

    @Column(nullable = false)
    public String description;

    @Column(nullable = false)
    public BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TransactionType type;

    @Column(nullable = false)
    public LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public UserEntity user;

    protected TransactionEntity() {
    }

    public TransactionEntity(String description, BigDecimal amount, TransactionType type, LocalDateTime date, UserEntity user) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.user = user;
    }
}

