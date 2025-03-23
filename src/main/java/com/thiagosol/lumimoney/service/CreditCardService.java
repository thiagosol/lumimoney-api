package com.thiagosol.lumimoney.service;

import com.thiagosol.lumimoney.dto.creditcard.GetCreditCardDTO;
import com.thiagosol.lumimoney.dto.creditcard.NewCreditCardDTO;
import com.thiagosol.lumimoney.entity.CreditCardEntity;
import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;
import com.thiagosol.lumimoney.entity.PaymentMethodEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CreditCardService {

    @Transactional
    public CreditCardEntity createCreditCard(PaymentMethodEntity paymentMethod, NewCreditCardDTO dto) {
        var creditCard = new CreditCardEntity(paymentMethod, dto.dueDayOfMonth(), dto.closingDayOfMonth(), dto.creditLimit());
        creditCard.persist();
        createInitialInvoices(creditCard, paymentMethod.getUserId());
        return creditCard;
    }

    private void createInitialInvoices(CreditCardEntity creditCard, UUID userId) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        
        // Se o dia de vencimento é depois do dia atual, cria fatura para o mês atual
        if (creditCard.getDueDayOfMonth() > today.getDayOfMonth()) {
            createInvoiceForMonth(creditCard, userId, currentMonth);
        }

        // Cria faturas para os próximos 2 meses
        createInvoiceForMonth(creditCard, userId, currentMonth.plusMonths(1));
        createInvoiceForMonth(creditCard, userId, currentMonth.plusMonths(2));
    }

    private void createInvoiceForMonth(CreditCardEntity creditCard, UUID userId, YearMonth yearMonth) {
        int dueDay = creditCard.getDueDayOfMonth();
        int closingDay = creditCard.getClosingDayOfMonth();
        
        // Ajusta o dia de vencimento se for maior que o último dia do mês
        LocalDate dueDate = yearMonth.atDay(Math.min(dueDay, yearMonth.lengthOfMonth()));
        
        // Calcula a data de fechamento no mesmo mês do vencimento
        LocalDate closingDate;
        if (closingDay < dueDay) {
            // Se o dia de fechamento é antes do vencimento, usa o mesmo mês
            closingDate = yearMonth.atDay(Math.min(closingDay, yearMonth.lengthOfMonth()));
        } else {
            // Se o dia de fechamento é depois ou igual ao vencimento, usa o mês anterior
            YearMonth closingMonth = yearMonth.minusMonths(1);
            closingDate = closingMonth.atDay(Math.min(closingDay, closingMonth.lengthOfMonth()));
        }

        var invoice = new CreditCardInvoiceEntity(dueDate, closingDate, false, BigDecimal.ZERO, creditCard, userId);
        invoice.persist();
    }

    public Optional<GetCreditCardDTO> getCreditCardByPaymentMethod(UUID paymentMethodId) {
        return CreditCardEntity.<CreditCardEntity>find("paymentMethod.id = ?1 and deleted = false", paymentMethodId)
                .firstResultOptional()
                .map(GetCreditCardDTO::new);
    }
} 
