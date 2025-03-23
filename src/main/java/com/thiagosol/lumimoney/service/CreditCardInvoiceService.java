package com.thiagosol.lumimoney.service;

import com.thiagosol.lumimoney.dto.creditcard.GetCreditCardInvoiceDTO;
import com.thiagosol.lumimoney.entity.CreditCardEntity;
import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;
import com.thiagosol.lumimoney.entity.TransactionEntity;
import com.thiagosol.lumimoney.entity.enums.TransactionFrequency;
import com.thiagosol.lumimoney.entity.enums.TransactionStatus;
import com.thiagosol.lumimoney.exception.InvoiceAlreadyPaidException;
import com.thiagosol.lumimoney.exception.InvoiceNotFoundException;
import com.thiagosol.lumimoney.exception.InvoiceNotPaidException;
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

@ApplicationScoped
public class CreditCardInvoiceService {

    @Inject
    CreditCardInvoiceRepository creditCardInvoiceRepository;

    @Inject
    AccountTransactionService accountTransactionService;

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

    public Optional<GetCreditCardInvoiceDTO> getFirstUnpaidInvoice(UUID creditCardId, UUID userId) {
        return CreditCardInvoiceEntity.<CreditCardInvoiceEntity>find(
                "creditCard.id = ?1 and userId = ?2 and isPaid = false and deleted = false order by dueDate asc",
                creditCardId, userId)
                .firstResultOptional()
                .map(GetCreditCardInvoiceDTO::new);
    }

    @Transactional
    public void payInvoice(UUID invoiceId, UUID userId) {
        CreditCardInvoiceEntity invoice = CreditCardInvoiceEntity.find(
                "id = ?1 and userId = ?2 and deleted = false",
                invoiceId, userId).firstResult();
        
        if (invoice == null) {
            throw new InvoiceNotFoundException();
        }

        if (invoice.isPaid()) {
            throw new InvoiceAlreadyPaidException();
        }

        invoice.pay();
        invoice.persist();

        accountTransactionService.processInvoicePayment(invoice, userId);

        List<CreditCardInvoiceEntity> futureInvoices = CreditCardInvoiceEntity
            .<CreditCardInvoiceEntity>list(
                "creditCard = ?1 and dueDate > ?2 and deleted = false order by dueDate asc",
                invoice.getCreditCard(), invoice.getDueDate());

        if (futureInvoices.size() <= 1 && !futureInvoices.isEmpty()) {
            createNextInvoice(invoice.getCreditCard(), userId, futureInvoices.get(0).getDueDate());
        }
    }

    @Transactional
    public void unpayInvoice(UUID invoiceId, UUID userId) {
        CreditCardInvoiceEntity invoice = CreditCardInvoiceEntity.find(
                "id = ?1 and userId = ?2 and deleted = false",
                invoiceId, userId).firstResult();
        
        if (invoice == null) {
            throw new InvoiceNotFoundException();
        }

        if (!invoice.isPaid()) {
            throw new InvoiceNotPaidException();
        }

        invoice.unpay();
        invoice.persist();

        accountTransactionService.processInvoiceUnpayment(invoice, userId);
    }

    @Transactional
    public CreditCardInvoiceEntity createNextInvoice(CreditCardEntity creditCard, UUID userId, LocalDate currentDueDate) {
        YearMonth nextMonth = YearMonth.from(currentDueDate).plusMonths(1);
        int safeDueDay = Math.min(creditCard.getDueDayOfMonth(), nextMonth.lengthOfMonth());
        LocalDate nextDueDate = nextMonth.atDay(safeDueDay);

        Optional<CreditCardInvoiceEntity> nextInvoice = CreditCardInvoiceEntity
            .<CreditCardInvoiceEntity>find("creditCard = ?1 and dueDate = ?2 and deleted = false", 
                creditCard, nextDueDate)
            .firstResultOptional();

        if (nextInvoice.isEmpty()) {
            LocalDate nextClosingDate;
            if (creditCard.getClosingDayOfMonth() < creditCard.getDueDayOfMonth()) {
                int safeClosingDay = Math.min(creditCard.getClosingDayOfMonth(), nextMonth.lengthOfMonth());
                nextClosingDate = nextMonth.atDay(safeClosingDay);
            } else {
                YearMonth closingMonth = nextMonth.minusMonths(1);
                int safeClosingDay = Math.min(creditCard.getClosingDayOfMonth(), closingMonth.lengthOfMonth());
                nextClosingDate = closingMonth.atDay(safeClosingDay);
            }

            var newInvoice = new CreditCardInvoiceEntity(nextDueDate, nextClosingDate, false, BigDecimal.ZERO,
                    creditCard, userId);
            newInvoice.persist();

            replicateFixedTransactions(currentDueDate, newInvoice, userId);

            return newInvoice;
        }

        return nextInvoice.get();
    }

    private void replicateFixedTransactions(LocalDate fromDueDate, CreditCardInvoiceEntity toInvoice, UUID userId) {

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
                userId
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
