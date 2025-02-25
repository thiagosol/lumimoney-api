package com.thiagosol.lumimoney.service;

import com.thiagosol.lumimoney.dto.transaction.GetTransactionDTO;
import com.thiagosol.lumimoney.dto.transaction.NewTransactionDTO;
import com.thiagosol.lumimoney.entity.TransactionEntity;
import com.thiagosol.lumimoney.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TransactionService {

    @Transactional
    public void createTransaction(NewTransactionDTO dto, UserEntity user) {
        TransactionEntity transaction = dto.toEntity(user);
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
            transactionOpt.get().delete();
            return true;
        }
        return false;
    }
}
