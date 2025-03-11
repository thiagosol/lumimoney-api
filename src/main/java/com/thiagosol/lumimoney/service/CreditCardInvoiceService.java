package com.thiagosol.lumimoney.service;

import com.thiagosol.lumimoney.dto.creditcard.GetCreditCardInvoiceDTO;
import com.thiagosol.lumimoney.entity.CreditCardEntity;
import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;
import com.thiagosol.lumimoney.entity.TransactionEntity;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.entity.enums.TransactionFrequency;
import com.thiagosol.lumimoney.entity.enums.TransactionStatus;
import com.thiagosol.lumimoney.repository.CreditCardInvoiceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.postgresql.core.TransactionState;

@ApplicationScoped
public class CreditCardInvoiceService {

    @Inject
    CreditCardInvoiceRepository creditCardInvoiceRepository;

    public List<GetCreditCardInvoiceDTO> getInvoicesByPaymentMethod(UUID paymentMethodId, Boolean isClosed) {
        String query = "creditCard.paymentMethod.id = ?1 and deleted = false";
        if (isClosed != null) {
            query += " and isClosed = ?2";
        }
        
        return CreditCardInvoiceEntity.<CreditCardInvoiceEntity>list(query, 
                isClosed != null ? new Object[]{paymentMethodId, isClosed} : new Object[]{paymentMethodId})
                .stream()
                .map(GetCreditCardInvoiceDTO::new)
                .toList();
    }

    public Optional<GetCreditCardInvoiceDTO> getFirstUnpaidInvoice(UUID creditCardId, UserEntity user) {
        return CreditCardInvoiceEntity.<CreditCardInvoiceEntity>find(
                "creditCard.id = ?1 and user = ?2 and isPaid = false and deleted = false order by dueDate asc",
                creditCardId, user)
                .firstResultOptional()
                .map(GetCreditCardInvoiceDTO::new);
    }

    @Transactional
    public void payInvoice(UUID invoiceId, UUID creditCardId, UserEntity user) {
        CreditCardInvoiceEntity invoice = CreditCardInvoiceEntity.find(
                "id = ?1 and creditCard.id = ?2 and user = ?3 and deleted = false",
                invoiceId, creditCardId, user).firstResult();
        
        if (invoice != null) {
            invoice.pay();
            invoice.persist();
        }
    }

    @Transactional
    public CreditCardInvoiceEntity createNextInvoice(CreditCardEntity creditCard, UserEntity user, LocalDate currentDueDate) {
        YearMonth nextMonth = YearMonth.from(currentDueDate).plusMonths(1);
        LocalDate nextDueDate = nextMonth.atDay(creditCard.getDueDayOfMonth());
        
        Optional<CreditCardInvoiceEntity> nextInvoice = CreditCardInvoiceEntity
            .<CreditCardInvoiceEntity>find("creditCard = ?1 and dueDate = ?2 and deleted = false", 
                creditCard, nextDueDate)
            .firstResultOptional();

        if (nextInvoice.isEmpty()) {
            LocalDate nextClosingDate;
            if (creditCard.getClosingDayOfMonth() < creditCard.getDueDayOfMonth()) {
                nextClosingDate = nextMonth.atDay(creditCard.getClosingDayOfMonth());
            } else {
                YearMonth closingMonth = nextMonth.minusMonths(1);
                nextClosingDate = closingMonth.atDay(creditCard.getClosingDayOfMonth());
            }

            var newInvoice = new CreditCardInvoiceEntity(nextDueDate, nextClosingDate, false, BigDecimal.ZERO,
                    creditCard, user);
            newInvoice.persist();

            replicateFixedTransactions(currentDueDate, newInvoice, user);

            return newInvoice;
        }

        return nextInvoice.get();
    }

    private void replicateFixedTransactions(LocalDate fromDueDate, CreditCardInvoiceEntity toInvoice, UserEntity user) {

        List<TransactionEntity> fixedTransactions = TransactionEntity
            .<TransactionEntity>list(
                "creditCardInvoice.creditCard = ?1 and creditCardInvoice.dueDate = ?2 and frequency = ?3 and deleted = false",
                toInvoice.getCreditCard(), fromDueDate, TransactionFrequency.FIXED);

        for (TransactionEntity transaction : fixedTransactions) {
            var newTransaction = new TransactionEntity(
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                TransactionFrequency.FIXED,
                TransactionStatus.PENDING,
                null,
                null,
                transaction.getRecurrenceId(),
                transaction.getPaymentMethod(),
                toInvoice,
                transaction.getDate(),
                user
            );
            newTransaction.persist();
        }
    }

    public List<CreditCardInvoiceEntity> getExistingFutureInvoices(CreditCardEntity creditCard, LocalDate fromDueDate) {
        return CreditCardInvoiceEntity
            .<CreditCardInvoiceEntity>list(
                "creditCard = ?1 and dueDate >= ?2 and deleted = false order by dueDate asc",
                creditCard, fromDueDate);
    }
}
