package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.transaction.TransactionLinkType;
import io.nuvalence.workmanager.service.repository.TransactionLinkTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

/**
 * Service layer to manage transaction link types.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class TransactionLinkTypeService {
    private final TransactionLinkTypeRepository repository;

    /**
     * Looks up a transaction link type by ID.
     *
     * @param id ID of transaction link type to find
     * @return Optional wrapping transaction
     */
    public Optional<TransactionLinkType> getTransactionLinkTypeById(final UUID id) {
        final Optional<TransactionLinkType> optional = repository.findById(id);

        if (optional.isPresent()) {
            final TransactionLinkType transactionLinkType = optional.get();
            return Optional.of(transactionLinkType);
        }

        return Optional.empty();
    }

    public TransactionLinkType saveTransactionLinkType(final TransactionLinkType transactionLinkType) {
        return repository.save(transactionLinkType);
    }

    public List<TransactionLinkType> getTransactionLinkTypes() {
        return (List<TransactionLinkType>) repository.findAll();
    }

    public void deleteTransactionLinkType(UUID id) {
        repository.deleteTransactionLinkTypeById(id);
    }
}
