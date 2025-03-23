package com.thiagosol.lumimoney.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.thiagosol.lumimoney.dto.transaction.GetMonthTransactionDTO;
import com.thiagosol.lumimoney.dto.transaction.GetTransactionDTO;
import com.thiagosol.lumimoney.dto.transaction.MonthTransactionsDTO;
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

    @Inject
    PaymentMethodService paymentMethodService;

    @Transactional
    public void createTransaction(NewTransactionDTO dto, UUID userId) {
        var paymentMethod = PaymentMethodEntity.<PaymentMethodEntity>findById(dto.paymentMethod());
        var creditCardInvoice = Optional.ofNullable(dto.creditCardInvoice())
                .map(id -> CreditCardInvoiceEntity.<CreditCardInvoiceEntity>findById(id)).orElse(null);

        if (creditCardInvoice != null && !creditCardInvoice.getCreditCard().getPaymentMethod().getId().equals(paymentMethod.getId())) {
            throw new InvoiceNotBelongToPaymentMethodException();
        }

        TransactionEntity transaction = null;
        switch (dto.frequency()) {
            case UNITARY -> transaction = createSingleTransaction(dto, paymentMethod, creditCardInvoice, userId);
            case INSTALLMENT -> createInstallments(dto, paymentMethod, creditCardInvoice, userId);
            case FIXED -> transaction = createFixedTransaction(dto, paymentMethod, creditCardInvoice, userId);
        }

        if (transaction != null && transaction.getStatus() == TransactionStatus.PAID && 
            transaction.getPaymentMethod().getType() == PaymentMethodType.ACCOUNT) {
            accountTransactionService.processTransactionPayment(transaction, userId);
        }
    }

    private TransactionEntity createSingleTransaction(NewTransactionDTO dto,
                                         PaymentMethodEntity paymentMethod,
                                         CreditCardInvoiceEntity creditCardInvoice,
                                         UUID userId) {
        TransactionEntity transaction = new TransactionEntity(dto.description(), dto.amount(), dto.type(),
                TransactionFrequency.UNITARY, dto.status(), null, null, null,
                paymentMethod, creditCardInvoice, dto.date(), userId);
        transaction.persist();
        return transaction;
    }

    private void createInstallments(NewTransactionDTO dto,
                                    PaymentMethodEntity paymentMethod,
                                    CreditCardInvoiceEntity creditCardInvoice,
                                    UUID userId) {
        var recurrenceId = UuidCreator.getTimeOrdered();
        var amount = dto.amount().divide(BigDecimal.valueOf(dto.totalInstallments()), 2, RoundingMode.HALF_UP);

        if (paymentMethod.getType() == PaymentMethodType.CREDIT_CARD) {
            CreditCardEntity creditCard = creditCardInvoice.getCreditCard();
            LocalDate currentDueDate = creditCardInvoice.getDueDate();

            for (int i = 1; i <= dto.totalInstallments(); i++) {
                var invoice = i == 1 ? creditCardInvoice : 
                    creditCardInvoiceService.createNextInvoice(creditCard, userId, currentDueDate);
                
                TransactionEntity transaction = new TransactionEntity(dto.description(), amount, dto.type(),
                        TransactionFrequency.INSTALLMENT, TransactionStatus.PAID, i, dto.totalInstallments(), recurrenceId,
                        paymentMethod, invoice, dto.date(), userId);
                transaction.persist();

                currentDueDate = invoice.getDueDate();
            }
        } else {
        for (int i = 1; i <= dto.totalInstallments(); i++) {
            var status = i == 1 ? dto.status() : TransactionStatus.PENDING;
            TransactionEntity transaction = new TransactionEntity(dto.description(), amount, dto.type(),
                    TransactionFrequency.INSTALLMENT, status, i, dto.totalInstallments(), recurrenceId,
                    paymentMethod, creditCardInvoice, dto.date(), userId);
            transaction.persist();

                // Se for a primeira parcela e estiver paga, processa o pagamento
                if (i == 1 && status == TransactionStatus.PAID) {
                    accountTransactionService.processTransactionPayment(transaction, userId);
                }
            }
        }
    }

    private TransactionEntity createFixedTransaction(NewTransactionDTO dto,
                                        PaymentMethodEntity paymentMethod,
                                        CreditCardInvoiceEntity creditCardInvoice,
                                        UUID userId) {
        var recurrenceId = UuidCreator.getTimeOrdered();

        if (paymentMethod.getType() == PaymentMethodType.CREDIT_CARD) {
            CreditCardEntity creditCard = creditCardInvoice.getCreditCard();
            
            List<CreditCardInvoiceEntity> futureInvoices = creditCardInvoiceService
                .getExistingFutureInvoices(creditCard, creditCardInvoice.getDueDate());

            for (CreditCardInvoiceEntity invoice : futureInvoices) {
                TransactionEntity transaction = new TransactionEntity(dto.description(), dto.amount(), dto.type(),
                        TransactionFrequency.FIXED, dto.status(), null, null, recurrenceId,
                        paymentMethod, invoice, dto.date(), userId);
                transaction.persist();
            }
            return null;
        } else {
        TransactionEntity transaction = new TransactionEntity(dto.description(), dto.amount(), dto.type(),
                TransactionFrequency.FIXED, dto.status(), null, null, recurrenceId,
                paymentMethod, creditCardInvoice, dto.date(), userId);
        transaction.persist();

            if (dto.status() == TransactionStatus.PAID) {
                createNextFixedTransaction(transaction);
            }

            return transaction;
        }
    }

    private void createNextFixedTransaction(TransactionEntity currentTransaction) {
        boolean hasNextTransaction = TransactionEntity.count(
            "recurrenceId = ?1 and status = ?2 and deleted = false",
            currentTransaction.getRecurrenceId(), TransactionStatus.PENDING) > 0;

        if (!hasNextTransaction) {

            TransactionEntity firstTransaction = TransactionEntity
                .<TransactionEntity>find("recurrenceId = ?1 and deleted = false order by date asc", 
                    currentTransaction.getRecurrenceId())
                .firstResult();

            YearMonth nextMonth = YearMonth.from(currentTransaction.getDate()).plusMonths(1);
            int safeDay = Math.min(firstTransaction.getDate().getDayOfMonth(), nextMonth.lengthOfMonth());
            LocalDateTime nextDate = nextMonth.atDay(safeDay).atStartOfDay();

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
                nextDate,
                currentTransaction.getUserId()
            );
            nextTransaction.persist();
        }
    }

    public List<GetTransactionDTO> getTransactionsByUser(UUID userId) {
        return TransactionEntity.<TransactionEntity>list("userId", userId)
                .stream().map(GetTransactionDTO::new)
                .toList();
    }

    public Optional<TransactionEntity> getTransactionById(Long id, UUID userId) {
        return TransactionEntity.find("id = ?1 and userId = ?2", id, userId).firstResultOptional();
    }

    @Transactional
    public boolean deleteTransaction(Long id, UUID userId) {
        Optional<TransactionEntity> transactionOpt = getTransactionById(id, userId);
        if (transactionOpt.isPresent()) {
            var transaction = transactionOpt.get();
            transaction.delete();
            transaction.persist();
            return true;
        }
        return false;
    }

    @Transactional
    public void unpayTransactions(List<Long> transactionIds, UUID userId) {
        for (Long id : transactionIds) {
            var transaction = getTransactionById(id, userId)
                .orElseThrow(TransactionNotFoundException::new);

            if (transaction.getPaymentMethod().getType() == PaymentMethodType.CREDIT_CARD) {
                throw new InvalidPaymentMethodTypeException();
            }

            if (transaction.getStatus() != TransactionStatus.PAID) {
                throw new TransactionNotPaidException();
            }

            transaction.setStatus(TransactionStatus.PENDING);
            transaction.persist();

            accountTransactionService.processTransactionUnpayment(transaction, userId);
        }
    }

    @Transactional
    public void payTransactions(List<Long> transactionIds, UUID userId) {
        for (Long id : transactionIds) {
            var transaction = getTransactionById(id, userId)
                .orElseThrow(TransactionNotFoundException::new);

            if (transaction.getPaymentMethod().getType() == PaymentMethodType.CREDIT_CARD) {
                throw new InvalidPaymentMethodTypeException();
            }

            if (transaction.getStatus() == TransactionStatus.PAID) {
                throw new TransactionAlreadyPaidException();
            }

            transaction.setStatus(TransactionStatus.PAID);
            transaction.persist();

            accountTransactionService.processTransactionPayment(transaction, userId);

            if (transaction.getFrequency() == TransactionFrequency.FIXED) {
                createNextFixedTransaction(transaction);
            }
        }
    }

    public MonthTransactionsDTO getTransactionsByMonth(UUID userId, YearMonth month, 
            TransactionType type, TransactionStatus status, PaymentMethodType paymentMethodType, UUID id) {
        
        LocalDateTime yearMonthNow = YearMonth.from(LocalDate.now()).atDay(1).atStartOfDay();
        LocalDateTime monthStart = month.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = month.atEndOfMonth().atTime(LocalTime.MAX);
        
        List<PaymentMethodEntity> accounts = paymentMethodService.getPaymentMethodsByType(userId, PaymentMethodType.ACCOUNT);
        
        if (paymentMethodType == PaymentMethodType.ACCOUNT) {
            accounts = accounts.stream()
                .filter(a -> a.getId().equals(id))
                .toList();
        }

        LocalDateTime minDate = monthStart.isBefore(yearMonthNow) ? monthStart : yearMonthNow;
    
        List<TransactionEntity> allTransactions = TransactionEntity
            .<TransactionEntity>find("userId = ?1 and date between ?2 and ?3 and deleted = false and creditCardInvoice is null",
                userId, minDate, monthEnd)
            .list();

        List<TransactionEntity> monthTransactions = allTransactions.stream().filter(t -> t.getDate().isAfter(monthStart)).toList();

        List<GetMonthTransactionDTO> virtualFixedTransactions = new ArrayList<>();
        List<GetMonthTransactionDTO> virtualFixedFutureTransactions = new ArrayList<>();

        List<TransactionEntity> fixedTransactions = TransactionEntity
            .<TransactionEntity>find("""
                select t from TransactionEntity t
                where t.userId = ?1
                and t.frequency = ?2
                and t.status = ?3
                and t.deleted = false
                and t.date <= ?4
                and t.creditCardInvoice is null
                and not exists (
                    select 1 from TransactionEntity t2
                    where t2.recurrenceId = t.recurrenceId
                    and t2.deleted = true
                )
                """, userId, TransactionFrequency.FIXED, TransactionStatus.PENDING, monthEnd)
            .list();

        Map<UUID, TransactionEntity> firstTransactionsByRecurrenceId = TransactionEntity
            .<TransactionEntity>find("""
                select t from TransactionEntity t
                where t.id in (
                    select min(t2.id) from TransactionEntity t2
                    where t2.recurrenceId in ?1
                    and t2.deleted = false
                    and t2.creditCardInvoice is null
                    group by t2.recurrenceId
                )
                """, fixedTransactions.stream()
                    .map(TransactionEntity::getRecurrenceId)
                    .toList())
            .stream()
            .collect(Collectors.toMap(
                TransactionEntity::getRecurrenceId,
                t -> t
            ));

        for (TransactionEntity fixed : fixedTransactions) {
            TransactionEntity firstTransaction = firstTransactionsByRecurrenceId.get(fixed.getRecurrenceId());

            boolean hasMonthTransaction = monthTransactions.stream()
                .anyMatch(t -> t.getRecurrenceId() != null && t.getRecurrenceId().equals(fixed.getRecurrenceId()));
            
            if (!hasMonthTransaction) {
                LocalDate monthDate = month.atDay(Math.min(firstTransaction.getDate().getDayOfMonth(), month.lengthOfMonth()));
                virtualFixedTransactions.add(new GetMonthTransactionDTO(fixed, monthDate));
            }

            if (!yearMonthNow.isAfter(monthEnd)) {
                YearMonth currentMonth = YearMonth.from(yearMonthNow);
                
                while (currentMonth.isBefore(month)) {
                    LocalDate currentDate = currentMonth.atDay(Math.min(firstTransaction.getDate().getDayOfMonth(), currentMonth.lengthOfMonth()));
                    virtualFixedFutureTransactions.add(new GetMonthTransactionDTO(fixed, currentDate));
                    currentMonth = currentMonth.plusMonths(1);
                }
            }
        }

        List<CreditCardEntity> creditCards = CreditCardEntity
            .<CreditCardEntity>find("paymentMethod.userId = ?1", userId)
            .list();

        if (paymentMethodType == PaymentMethodType.CREDIT_CARD) {
            creditCards = creditCards.stream()
                .filter(c -> c.getPaymentMethod().getId().equals(id))
                .toList();
        }

        List<GetMonthTransactionDTO> monthCreditCardInvoices = new ArrayList<>();
        List<GetMonthTransactionDTO> futureCreditCardInvoices = new ArrayList<>();

        for (CreditCardEntity card : creditCards) {

            List<CreditCardInvoiceEntity> allInvoices = CreditCardInvoiceEntity
            .<CreditCardInvoiceEntity>find("creditCard = ?1 and dueDate between ?2 and ?3",
                card, minDate.toLocalDate(), monthEnd.toLocalDate())
            .list();

            Optional<CreditCardInvoiceEntity> monthInvoice = allInvoices.stream()
                .filter(i -> i.getDueDate().isAfter(monthStart.toLocalDate()) && i.getDueDate().isBefore(monthEnd.toLocalDate()))
                .findFirst();

            if (monthInvoice.isPresent()) {
                var statusCreditCardInvoice = monthInvoice.get().isPaid() ? TransactionStatus.PAID : TransactionStatus.PENDING;
                BigDecimal total = calculateInvoiceAmount(monthInvoice.get());
                monthCreditCardInvoices.add(GetMonthTransactionDTO.fromCreditCardInvoice(
                    card.getId(),
                    card.getPaymentMethod().getName(),
                    total,
                    monthInvoice.get().getDueDate(),
                    statusCreditCardInvoice
                ));
            }

            if (!yearMonthNow.isAfter(monthEnd)) {
                for (CreditCardInvoiceEntity invoice : allInvoices) {
                    var statusCreditCardInvoice = invoice.isPaid() ? TransactionStatus.PAID : TransactionStatus.PENDING;
                    BigDecimal total = calculateInvoiceAmount(invoice);
                    futureCreditCardInvoices.add(GetMonthTransactionDTO.fromCreditCardInvoice(
                        card.getId(),
                        card.getPaymentMethod().getName(),
                        total,
                        invoice.getDueDate(),
                        statusCreditCardInvoice
                    ));
                }
            }
        }

        List<GetMonthTransactionDTO> allMonthTransactions = new ArrayList<>();
        allMonthTransactions.addAll(monthTransactions.stream().map(GetMonthTransactionDTO::new).toList());
        allMonthTransactions.addAll(virtualFixedTransactions);
        allMonthTransactions.addAll(monthCreditCardInvoices);

        List<GetMonthTransactionDTO> allFutureTransactions = new ArrayList<>();
        allFutureTransactions.addAll(allTransactions.stream().map(GetMonthTransactionDTO::new).toList());
        allFutureTransactions.addAll(virtualFixedFutureTransactions);
        allFutureTransactions.addAll(futureCreditCardInvoices);

        List<GetMonthTransactionDTO> filteredTransactions = allMonthTransactions.stream()
            .filter(t -> type == null || t.type() == type)
            .filter(t -> status == null || t.status() == status)
            .sorted(Comparator.comparing(GetMonthTransactionDTO::date))
            .toList();

        List<MonthTransactionsDTO.AccountBalanceDTO> accountsBalance = new ArrayList<>();
        if (paymentMethodType == null || paymentMethodType == PaymentMethodType.ACCOUNT) {
            for (PaymentMethodEntity account : accounts) {
                AccountEntity accountEntity = AccountEntity.<AccountEntity>find("paymentMethod", account).firstResult();
                BigDecimal currentBalance = accountEntity.getBalance();
                
                BigDecimal income = calculateMonthlyAmount(allMonthTransactions, account.getId(), TransactionType.INCOME);
                BigDecimal expense = calculateMonthlyAmount(allMonthTransactions, account.getId(), TransactionType.EXPENSE);
                
                BigDecimal pendingIncome = calculatePendingAmount(allFutureTransactions, account.getId(), TransactionType.INCOME);
                BigDecimal pendingExpense = calculatePendingAmount(allFutureTransactions, account.getId(), TransactionType.EXPENSE);
                
                BigDecimal expected = currentBalance.add(pendingIncome).subtract(pendingExpense);

                accountsBalance.add(new MonthTransactionsDTO.AccountBalanceDTO(
                    account.getId(),
                    account.getName(),
                    currentBalance,
                    income,
                    expense,
                    expected
                ));
            }
        }

        return new MonthTransactionsDTO(filteredTransactions, accountsBalance);
    }

    private BigDecimal calculateMonthlyAmount(List<GetMonthTransactionDTO> transactions, UUID accountId, TransactionType type) {
        return transactions.stream()
            .filter(t -> t.paymentMethodId().equals(accountId))
            .filter(t -> t.type() == type)
            .map(GetMonthTransactionDTO::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePendingAmount(List<GetMonthTransactionDTO> transactions, UUID accountId, TransactionType type) {
        return transactions.stream()
            .filter(t -> t.paymentMethodId().equals(accountId))
            .filter(t -> t.type() == type)
            .filter(t -> t.status() == TransactionStatus.PENDING)
            .map(GetMonthTransactionDTO::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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



