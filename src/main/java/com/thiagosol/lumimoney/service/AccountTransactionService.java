package com.thiagosol.lumimoney.service;

import com.thiagosol.lumimoney.entity.AccountEntity;
import com.thiagosol.lumimoney.entity.AccountTransactionEntity;
import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;
import com.thiagosol.lumimoney.entity.TransactionEntity;
import com.thiagosol.lumimoney.entity.enums.AccountTransactionType;
import com.thiagosol.lumimoney.entity.enums.TransactionType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@ApplicationScoped
public class AccountTransactionService {

    @Transactional
    public void processTransactionPayment(TransactionEntity transaction, UUID userId) {
        AccountEntity account = AccountEntity.find("paymentMethod", transaction.getPaymentMethod()).firstResult();
        AccountTransactionType operationType = getOperationTypeForTransaction(transaction.getType());

        var previousOperation = findLastOperation(account, transaction, null);
        var accountTransaction = new AccountTransactionEntity(
            account, transaction, null, transaction.getAmount(),
            operationType, userId);
        accountTransaction.setPreviousOperation(previousOperation);

        accountTransaction.persist();
        updateAccountBalance(account, accountTransaction.getCurrentBalance());
    }

    @Transactional
    public void processTransactionUnpayment(TransactionEntity transaction, UUID userId) {
        AccountEntity account = AccountEntity.find("paymentMethod", transaction.getPaymentMethod()).firstResult();
        // Inverte a operação: se era débito vira crédito e vice-versa
        AccountTransactionType operationType = getOperationTypeForTransaction(transaction.getType()).equals(AccountTransactionType.DEBIT) 
            ? AccountTransactionType.CREDIT 
            : AccountTransactionType.DEBIT;

        var previousOperation = findLastOperation(account, transaction, null);
        var accountTransaction = new AccountTransactionEntity(
            account, transaction, null, transaction.getAmount(),
            operationType, userId);
        accountTransaction.setPreviousOperation(previousOperation);

        accountTransaction.persist();
        updateAccountBalance(account, accountTransaction.getCurrentBalance());
    }

    @Transactional
    public void processInvoicePayment(CreditCardInvoiceEntity invoice, UUID userId) {
        AccountEntity account = AccountEntity.find("paymentMethod", invoice.getCreditCard().getPaymentMethod()).firstResult();
        
        var previousOperation = findLastOperation(account, null, invoice);
        var accountTransaction = new AccountTransactionEntity(
            account, null, invoice, invoice.getTotalAmount(),
            AccountTransactionType.DEBIT, userId);
        accountTransaction.setPreviousOperation(previousOperation);

        accountTransaction.persist();
        updateAccountBalance(account, accountTransaction.getCurrentBalance());
    }

    @Transactional
    public void processInvoiceUnpayment(CreditCardInvoiceEntity invoice, UUID userId) {
        AccountEntity account = AccountEntity.find("paymentMethod", invoice.getCreditCard().getPaymentMethod()).firstResult();
        
        var previousOperation = findLastOperation(account, null, invoice);
        var accountTransaction = new AccountTransactionEntity(
            account, null, invoice, invoice.getTotalAmount(),
            AccountTransactionType.CREDIT, userId);
        accountTransaction.setPreviousOperation(previousOperation);

        accountTransaction.persist();
        updateAccountBalance(account, accountTransaction.getCurrentBalance());
    }

    private AccountTransactionType getOperationTypeForTransaction(TransactionType type) {
        return switch (type) {
            case EXPENSE -> AccountTransactionType.DEBIT;
            case INCOME -> AccountTransactionType.CREDIT;
        };
    }

    private void updateAccountBalance(AccountEntity account, BigDecimal newBalance) {
        account.setBalance(newBalance);
        account.persist();
    }

    private AccountTransactionEntity findLastOperation(AccountEntity account, TransactionEntity transaction, CreditCardInvoiceEntity invoice) {
        String query = "account = ?1";
        Object[] params;

        if (transaction != null) {
            query += " and transaction = ?2";
            params = new Object[]{account, transaction};
        } else if (invoice != null) {
            query += " and creditCardInvoice = ?2";
            params = new Object[]{account, invoice};
        } else {
            params = new Object[]{account};
        }

        query += " order by date desc";

        return AccountTransactionEntity.<AccountTransactionEntity>find(query, params)
            .firstResult();
    }
}
