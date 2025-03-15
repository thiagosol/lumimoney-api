package com.thiagosol.lumimoney.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.thiagosol.lumimoney.dto.transaction.GetMonthTransactionDTO;
import com.thiagosol.lumimoney.dto.transaction.GetTransactionDTO;
import com.thiagosol.lumimoney.dto.transaction.NewTransactionDTO;
import com.thiagosol.lumimoney.entity.*;
import com.thiagosol.lumimoney.entity.enums.PaymentMethodType;
import com.thiagosol.lumimoney.entity.enums.TransactionFrequency;
import com.thiagosol.lumimoney.entity.enums.TransactionStatus;
import com.thiagosol.lumimoney.entity.enums.TransactionType;
import com.thiagosol.lumimoney.exception.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class TransactionService {

    @Inject
    CreditCardInvoiceService creditCardInvoiceService;

    @Inject
    AccountTransactionService accountTransactionService;

    @Transactional
    public void createTransaction(NewTransactionDTO dto, UserEntity user) {
        var paymentMethod = PaymentMethodEntity.<PaymentMethodEntity>findById(dto.paymentMethod());
        var creditCardInvoice = Optional.ofNullable(dto.creditCardInvoice())
                .map(id -> CreditCardInvoiceEntity.<CreditCardInvoiceEntity>findById(id)).orElse(null);

        if (creditCardInvoice != null && !creditCardInvoice.getCreditCard().getPaymentMethod().getId().equals(paymentMethod.getId())) {
            throw new InvoiceNotBelongToPaymentMethodException();
        }

        TransactionEntity transaction = null;
        switch (dto.frequency()) {
            case UNITARY -> transaction = createSingleTransaction(dto, paymentMethod, creditCardInvoice, user);
            case INSTALLMENT -> createInstallments(dto, paymentMethod, creditCardInvoice, user);
            case FIXED -> transaction = createFixedTransaction(dto, paymentMethod, creditCardInvoice, user);
        }

        if (transaction != null && transaction.getStatus() == TransactionStatus.PAID && 
            transaction.getPaymentMethod().getType() == PaymentMethodType.ACCOUNT) {
            accountTransactionService.processTransactionPayment(transaction, user);
        }
    }

    private TransactionEntity createSingleTransaction(NewTransactionDTO dto,
                                         PaymentMethodEntity paymentMethod,
                                         CreditCardInvoiceEntity creditCardInvoice,
                                         UserEntity user) {
        TransactionEntity transaction = new TransactionEntity(dto.description(), dto.amount(), dto.type(),
                TransactionFrequency.UNITARY, dto.status(), null, null, null,
                paymentMethod, creditCardInvoice, dto.date(), user);
        transaction.persist();
        return transaction;
    }

    private void createInstallments(NewTransactionDTO dto,
                                    PaymentMethodEntity paymentMethod,
                                    CreditCardInvoiceEntity creditCardInvoice,
                                    UserEntity user) {
        var recurrenceId = UuidCreator.getTimeOrdered();
        var amount = dto.amount().divide(BigDecimal.valueOf(dto.totalInstallments()), 2, RoundingMode.HALF_UP);

        if (paymentMethod.getType() == PaymentMethodType.CREDIT_CARD) {
            CreditCardEntity creditCard = creditCardInvoice.getCreditCard();
            LocalDate currentDueDate = creditCardInvoice.getDueDate();

            for (int i = 1; i <= dto.totalInstallments(); i++) {
                var invoice = i == 1 ? creditCardInvoice : 
                    creditCardInvoiceService.createNextInvoice(creditCard, user, currentDueDate);
                
                TransactionEntity transaction = new TransactionEntity(dto.description(), amount, dto.type(),
                        TransactionFrequency.INSTALLMENT, TransactionStatus.PAID, i, dto.totalInstallments(), recurrenceId,
                        paymentMethod, invoice, dto.date(), user);
                transaction.persist();

                currentDueDate = invoice.getDueDate();
            }
        } else {
            for (int i = 1; i <= dto.totalInstallments(); i++) {
                var status = i == 1 ? dto.status() : TransactionStatus.PENDING;
                TransactionEntity transaction = new TransactionEntity(dto.description(), amount, dto.type(),
                        TransactionFrequency.INSTALLMENT, status, i, dto.totalInstallments(), recurrenceId,
                        paymentMethod, creditCardInvoice, dto.date(), user);
                transaction.persist();

                // Se for a primeira parcela e estiver paga, processa o pagamento
                if (i == 1 && status == TransactionStatus.PAID) {
                    accountTransactionService.processTransactionPayment(transaction, user);
                }
            }
        }
    }

    private TransactionEntity createFixedTransaction(NewTransactionDTO dto,
                                        PaymentMethodEntity paymentMethod,
                                        CreditCardInvoiceEntity creditCardInvoice,
                                        UserEntity user) {
        var recurrenceId = UuidCreator.getTimeOrdered();

        if (paymentMethod.getType() == PaymentMethodType.CREDIT_CARD) {
            CreditCardEntity creditCard = creditCardInvoice.getCreditCard();
            
            List<CreditCardInvoiceEntity> futureInvoices = creditCardInvoiceService
                .getExistingFutureInvoices(creditCard, creditCardInvoice.getDueDate());

            for (CreditCardInvoiceEntity invoice : futureInvoices) {
                TransactionEntity transaction = new TransactionEntity(dto.description(), dto.amount(), dto.type(),
                        TransactionFrequency.FIXED, dto.status(), null, null, recurrenceId,
                        paymentMethod, invoice, dto.date(), user);
                transaction.persist();
            }
            return null;
        } else {
            TransactionEntity transaction = new TransactionEntity(dto.description(), dto.amount(), dto.type(),
                    TransactionFrequency.FIXED, dto.status(), null, null, recurrenceId,
                    paymentMethod, creditCardInvoice, dto.date(), user);
            transaction.persist();
            return transaction;
        }
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

    @Transactional
    public void unpayTransactions(List<Long> transactionIds, UserEntity user) {
        for (Long id : transactionIds) {
            var transaction = getTransactionById(id, user)
                .orElseThrow(TransactionNotFoundException::new);

            if (transaction.getPaymentMethod().getType() == PaymentMethodType.CREDIT_CARD) {
                throw new InvalidPaymentMethodTypeException();
            }

            if (transaction.getStatus() != TransactionStatus.PAID) {
                throw new TransactionNotPaidException();
            }

            transaction.setStatus(TransactionStatus.PENDING);
            transaction.persist();

            accountTransactionService.processTransactionUnpayment(transaction, user);
        }
    }

    @Transactional
    public void payTransactions(List<Long> transactionIds, UserEntity user) {
        for (Long id : transactionIds) {
            var transaction = getTransactionById(id, user)
                .orElseThrow(TransactionNotFoundException::new);

            if (transaction.getPaymentMethod().getType() == PaymentMethodType.CREDIT_CARD) {
                throw new InvalidPaymentMethodTypeException();
            }

            if (transaction.getStatus() == TransactionStatus.PAID) {
                throw new TransactionAlreadyPaidException();
            }

            transaction.setStatus(TransactionStatus.PAID);
            transaction.persist();

            accountTransactionService.processTransactionPayment(transaction, user);

            if (transaction.getFrequency() == TransactionFrequency.FIXED) {
                createNextFixedTransaction(transaction);
            }
        }
    }

    private void createNextFixedTransaction(TransactionEntity currentTransaction) {
        boolean hasNextTransaction = TransactionEntity.count(
            "recurrenceId = ?1 and status = ?2 and deleted = false",
            currentTransaction.getRecurrenceId(), TransactionStatus.PENDING) > 0;

        if (!hasNextTransaction) {
            var nextTransaction = new TransactionEntity(
                currentTransaction.getDescription(),
                currentTransaction.getAmount(),
                currentTransaction.getType(),
                TransactionFrequency.FIXED,
                TransactionStatus.PENDING,
                null,
                null,
                currentTransaction.getRecurrenceId(),
                currentTransaction.getPaymentMethod(),
                null,
                currentTransaction.getDate().plusMonths(1),
                currentTransaction.getUser()
            );
            nextTransaction.persist();
        }
    }

    public List<GetMonthTransactionDTO> getTransactionsByMonth(UserEntity user, YearMonth month, 
            TransactionType type, TransactionStatus status) {
        List<GetMonthTransactionDTO> result = new ArrayList<>();
        
        LocalDateTime startDate = month.atDay(1).atStartOfDay();
        LocalDateTime endDate = month.atEndOfMonth().atTime(LocalTime.MAX);
        
        String query = "user = ?1 and date between ?2 and ?3 and deleted = false and paymentMethod.type = ?4";
        List<Object> params = new ArrayList<>(Arrays.asList(user, startDate, endDate, PaymentMethodType.ACCOUNT));

        if (type != null) {
            query += " and type = ?5";
            params.add(type);
        }
        if (status != null) {
            query += " and status = ?6";
            params.add(status);
        }

        List<TransactionEntity> monthTransactions = TransactionEntity
            .<TransactionEntity>find(query + " order by date asc", params.toArray())
            .list();
        
        result.addAll(monthTransactions.stream()
            .map(GetMonthTransactionDTO::new)
            .toList());

        if (status == null || status == TransactionStatus.PENDING) {
            String fixedQuery = "user = ?1 and frequency = ?2 and status = ?3 and deleted = false " +
                "and date < ?4 and paymentMethod.type = ?5";
            if (type != null) {
                fixedQuery += " and type = ?6";
            }

            List<Object> fixedParams = new ArrayList<>(Arrays.asList(
                user, TransactionFrequency.FIXED, TransactionStatus.PENDING, startDate, PaymentMethodType.ACCOUNT
            ));
            if (type != null) {
                fixedParams.add(type);
            }

            List<TransactionEntity> fixedTransactions = TransactionEntity
                .<TransactionEntity>find(fixedQuery, fixedParams.toArray())
                .list();

            result.addAll(fixedTransactions.stream()
                .filter(t -> !result.stream().map(GetMonthTransactionDTO::recurrenceId).collect(Collectors.toSet()).contains(t.getRecurrenceId()))
                .map(t -> new GetMonthTransactionDTO(t, startDate.toLocalDate().withDayOfMonth(t.getDate().getDayOfMonth())))
                .toList());
        }

        if (type == null || type == TransactionType.EXPENSE) {

            List<CreditCardEntity> creditCards = CreditCardEntity
                .<CreditCardEntity>find("paymentMethod.user", user)
                .list();

            for (CreditCardEntity card : creditCards) {

                Optional<CreditCardInvoiceEntity> invoice = CreditCardInvoiceEntity
                    .<CreditCardInvoiceEntity>find("creditCard = ?1 and dueDate between ?2 and ?3",
                        card, startDate.toLocalDate(), endDate.toLocalDate())
                    .firstResultOptional();

                if (invoice.isPresent()) {                    
                    var statusCreditCardInvoice = invoice.get().isPaid() ? TransactionStatus.PAID : TransactionStatus.PENDING;
                    if (status == null || status == statusCreditCardInvoice) {
                        BigDecimal total = calculateInvoiceAmount(invoice.get());
                        result.add(GetMonthTransactionDTO.fromCreditCardInvoice(
                            card.getId(),
                            card.getPaymentMethod().getName(),
                            total,
                            invoice.get().getDueDate(),
                            statusCreditCardInvoice
                        ));
                    }
                }
            }
        }

        return result.stream()
            .sorted(Comparator.comparing(GetMonthTransactionDTO::date))
            .toList();
    }

    private BigDecimal calculateInvoiceAmount(CreditCardInvoiceEntity invoice) {
        return TransactionEntity.<TransactionEntity>find(
                "creditCardInvoice = ?1 and deleted = false", invoice)
            .stream()
            .map(t -> t.getType() == TransactionType.EXPENSE ? 
                t.getAmount() : 
                t.getAmount().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
