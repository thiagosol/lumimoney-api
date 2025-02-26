package com.thiagosol.lumimoney.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.thiagosol.lumimoney.dto.transaction.GetTransactionDTO;
import com.thiagosol.lumimoney.dto.transaction.NewTransactionDTO;
import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;
import com.thiagosol.lumimoney.entity.PaymentMethodEntity;
import com.thiagosol.lumimoney.entity.TransactionEntity;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.entity.enums.TransactionFrequency;
import com.thiagosol.lumimoney.entity.enums.TransactionStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TransactionService {

    @Transactional
    public void createTransaction(NewTransactionDTO dto, UserEntity user) {
        var paymentMethod = PaymentMethodEntity.<PaymentMethodEntity>findById(dto.paymentMethod());
        var creditCardInvoice = Optional.ofNullable(dto.creditCardInvoice())
                .map(id -> CreditCardInvoiceEntity.<CreditCardInvoiceEntity>findById(id)).orElse(null);

        switch (dto.frequency()) {
            case TransactionFrequency.UNITARY -> createSingleTransaction(dto, paymentMethod, creditCardInvoice, user);
            case TransactionFrequency.INSTALLMENT -> createInstallments(dto, paymentMethod, creditCardInvoice, user);
            case TransactionFrequency.FIXED -> createFixedTransaction(dto, paymentMethod, creditCardInvoice, user);
        }
    }

    private void createSingleTransaction(NewTransactionDTO dto,
                                         PaymentMethodEntity paymentMethod,
                                         CreditCardInvoiceEntity creditCardInvoice,
                                         UserEntity user) {
        TransactionEntity transaction = new TransactionEntity(dto.description(), dto.amount(), dto.type(),
                TransactionFrequency.UNITARY, dto.status(), null, null, null,
                paymentMethod, creditCardInvoice, dto.date(), user);
        transaction.persist();
    }

    private void createInstallments(NewTransactionDTO dto,
                                    PaymentMethodEntity paymentMethod,
                                    CreditCardInvoiceEntity creditCardInvoice,
                                    UserEntity user) {
        var recurrenceId = UuidCreator.getTimeOrdered();
        var amount = dto.amount().divide(BigDecimal.valueOf(dto.totalInstallments()), 2, RoundingMode.HALF_UP);
        for (int i = 1; i <= dto.totalInstallments(); i++) {
            var status = i == 1 ? dto.status() : TransactionStatus.PENDING;
            TransactionEntity transaction = new TransactionEntity(dto.description(), amount, dto.type(),
                    TransactionFrequency.INSTALLMENT, status, i, dto.totalInstallments(), recurrenceId,
                    paymentMethod, creditCardInvoice, dto.date(), user);
            transaction.persist();
        }
    }

    private void createFixedTransaction(NewTransactionDTO dto,
                                        PaymentMethodEntity paymentMethod,
                                        CreditCardInvoiceEntity creditCardInvoice,
                                        UserEntity user) {
        var recurrenceId = UuidCreator.getTimeOrdered();
        TransactionEntity transaction = new TransactionEntity(dto.description(), dto.amount(), dto.type(),
                TransactionFrequency.FIXED, dto.status(), null, null, recurrenceId,
                paymentMethod, creditCardInvoice, dto.date(), user);
        transaction.persist();
    }

    public List<GetTransactionDTO> getTransactionsByUser(UserEntity user) {
        return TransactionEntity.<TransactionEntity>list("user", user)
                .stream().map(GetTransactionDTO::new)
                .toList();
    }

    public Optional<TransactionEntity> getTransactionById(Long id, UserEntity user) {
        return TransactionEntity.find("id = ?1 and user = ?2", id, user).firstResultOptional();
    }

    @Transactional
    public boolean deleteTransaction(Long id, UserEntity user) {
        Optional<TransactionEntity> transactionOpt = getTransactionById(id, user);
        if (transactionOpt.isPresent()) {
            var transaction = transactionOpt.get();
            transaction.delete();
            transaction.persist();
            return true;
        }
        return false;
    }
}
