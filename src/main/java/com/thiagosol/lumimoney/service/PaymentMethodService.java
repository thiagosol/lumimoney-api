package com.thiagosol.lumimoney.service;

import com.thiagosol.lumimoney.dto.paymentmethod.GetPaymentMethodDTO;
import com.thiagosol.lumimoney.dto.paymentmethod.NewPaymentMethodDTO;
import com.thiagosol.lumimoney.entity.PaymentMethodEntity;
import com.thiagosol.lumimoney.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PaymentMethodService {

    @Transactional
    public void createPaymentMethod(NewPaymentMethodDTO dto, UserEntity user) {
        var paymentMethodEntity = new PaymentMethodEntity(dto.name(), dto.type(), user);
        paymentMethodEntity.persist();
    }

    public List<GetPaymentMethodDTO> getPaymentMethodsByUser(UserEntity user) {
        return PaymentMethodEntity.<PaymentMethodEntity>list("user", user)
                .stream().map(GetPaymentMethodDTO::new)
                .toList();
    }

    public Optional<PaymentMethodEntity> getPaymentMethodById(Long id, UserEntity user) {
        return PaymentMethodEntity.find("id = ?1 and user = ?2", id, user).firstResultOptional();
    }

    @Transactional
    public boolean deletePaymentMethod(Long id, UserEntity user) {
        Optional<PaymentMethodEntity> optionalPaymentMethod = getPaymentMethodById(id, user);
        if (optionalPaymentMethod.isPresent()) {
            var paymentMethod = optionalPaymentMethod.get();
            paymentMethod.delete();
            paymentMethod.persist();
            return true;
        }
        return false;
    }
}
