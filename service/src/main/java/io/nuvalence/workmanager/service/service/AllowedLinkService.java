package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.transaction.AllowedLink;
import io.nuvalence.workmanager.service.domain.transaction.TransactionLinkType;
import io.nuvalence.workmanager.service.repository.AllowedLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

/**
 * Service layer to manage allowed links.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class AllowedLinkService {
    private final AllowedLinkRepository repository;
    private final TransactionLinkTypeService transactionLinkTypeService;

    /**
     * Create a new allowed link.
     *
     * @param allowedLink The allowed link to be created.
     * @param transactionLinkTypeId The transaction link type to map to.
     * @return The newly created allowed link
     */
    public AllowedLink saveAllowedLink(final AllowedLink allowedLink, UUID transactionLinkTypeId) {
        final TransactionLinkType linkType = transactionLinkTypeService
                .getTransactionLinkTypeById(transactionLinkTypeId)
                .orElse(null);
        allowedLink.setTransactionLinkType(linkType);
        return repository.save(allowedLink);
    }

    /**
     * Get the allowed links associated with a definition.
     *
     * @param definitionKey The definition key to search on.
     * @return List of allowed links related to a definition.
     */
    public List<AllowedLink> getAllowedLinksByDefinitionKey(String definitionKey) {
        return repository.findByDefinitionKey(definitionKey);
    }
}