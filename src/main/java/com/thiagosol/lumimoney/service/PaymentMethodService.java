package com.thiagosol.lumimoney.service;

import com.thiagosol.lumimoney.dto.paymentmethod.GetPaymentMethodDTO;
import com.thiagosol.lumimoney.dto.paymentmethod.NewPaymentMethodDTO;
import com.thiagosol.lumimoney.entity.AccountEntity;
import com.thiagosol.lumimoney.entity.CreditCardEntity;
import com.thiagosol.lumimoney.entity.PaymentMethodEntity;
import com.thiagosol.lumimoney.entity.enums.PaymentMethodType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PaymentMethodService {

    @Inject
    CreditCardInvoiceService creditCardInvoiceService;

    @Inject
    AccountService accountService;

    @Inject
    CreditCardService creditCardService;

    @Transactional
    public void createPaymentMethod(NewPaymentMethodDTO dto, UUID userId) {
        var paymentMethodEntity = new PaymentMethodEntity(dto.name(), dto.type(), userId);
        paymentMethodEntity.persist();

        if (dto.type() == PaymentMethodType.ACCOUNT) {
            accountService.createAccount(paymentMethodEntity, dto.account());
        } else if (dto.type() == PaymentMethodType.CREDIT_CARD) {
            creditCardService.createCreditCard(paymentMethodEntity, dto.creditCard());
        }
    }

    public List<GetPaymentMethodDTO> getPaymentMethodsByUser(UUID userId) {
        return PaymentMethodEntity.<PaymentMethodEntity>list("userId = ?1 and deleted = false", userId)
                .stream()
                .map(paymentMethod -> {
                    if (paymentMethod.getType() == PaymentMethodType.CREDIT_CARD) {
                        var creditCard = CreditCardEntity.<CreditCardEntity>find("paymentMethod = ?1 and deleted = false", paymentMethod)
                                .firstResult();
                        if (creditCard != null) {
                            var firstUnpaidInvoice = creditCardInvoiceService.getFirstUnpaidInvoice(creditCard.getId(), userId);
                            return new GetPaymentMethodDTO(paymentMethod, firstUnpaidInvoice.orElse(null));
                        }
                    } else if (paymentMethod.getType() == PaymentMethodType.ACCOUNT) {
                        var account = AccountEntity.<AccountEntity>find("paymentMethod = ?1 and deleted = false", paymentMethod)
                                .firstResult();
                        return new GetPaymentMethodDTO(paymentMethod, account);
                    }
                    return new GetPaymentMethodDTO(paymentMethod);
                })
                .toList();
    }

    public Optional<PaymentMethodEntity> getPaymentMethodById(Long id, UUID userId) {
        return PaymentMethodEntity.find("id = ?1 and userId = ?2", id, userId).firstResultOptional();
    }

    @Transactional
    public boolean deletePaymentMethod(Long id, UUID userId) {
        Optional<PaymentMethodEntity> optionalPaymentMethod = getPaymentMethodById(id, userId);
        if (optionalPaymentMethod.isPresent()) {
            var paymentMethod = optionalPaymentMethod.get();
            paymentMethod.delete();
            paymentMethod.persist();

            if (paymentMethod.getType() == PaymentMethodType.ACCOUNT) {
                AccountEntity account = AccountEntity.find("paymentMethod = ?1", paymentMethod).firstResult();
                if (account != null) {
                    account.delete();
                    account.persist();
                }
            } else if (paymentMethod.getType() == PaymentMethodType.CREDIT_CARD) {
                CreditCardEntity creditCard = CreditCardEntity.find("paymentMethod = ?1", paymentMethod).firstResult();
                if (creditCard != null) {
                    creditCard.delete();
                    creditCard.persist();
                }
            }
            return true;
        }
        return false;
    }

    public List<PaymentMethodEntity> getPaymentMethodsByType(UUID userId, PaymentMethodType type) {
        return PaymentMethodEntity.<PaymentMethodEntity>find("userId = ?1 and type = ?2", userId, type).list();
    }
}
