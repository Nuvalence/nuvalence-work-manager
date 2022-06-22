package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.transaction.AllowedLink;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTransactionDefinitionException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.domain.transaction.TransactionLink;
import io.nuvalence.workmanager.service.domain.transaction.TransactionLinkNotAllowedException;
import io.nuvalence.workmanager.service.domain.transaction.TransactionLinkType;
import io.nuvalence.workmanager.service.generated.models.LinkedTransaction;
import io.nuvalence.workmanager.service.repository.TransactionLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;

/**
 * Service layer to manage transaction links.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class TransactionLinkService {
    private final TransactionDefinitionService transactionDefinitionService;
    private final AllowedLinkService allowedLinkService;
    private final TransactionService transactionService;
    private final TransactionLinkRepository repository;

    private final TransactionLinkTypeService transactionLinkTypeService;

    /**
     * Create a link between two transactions.
     *  Get the TransactionDefinition of the 2 Transactions by id
     *  If both transactions have the same definition then o.k.
     *  Else lookup definitions and make sure that there is an AllowedLink that associates the 2
     *
     * @param transactionLink Transaction link request
     * @param transactionLinkTypeId The transaction link type to map t0
     * @return Created transaction link
     * @throws MissingEntityException If any of the transactions reference missing entities
     * @throws MissingTransactionDefinitionException If any of the transactions have missing transaction definition
     * @throws TransactionLinkNotAllowedException If the two transactions are not allowed to be linked
     */
    public TransactionLink saveTransactionLink(TransactionLink transactionLink, UUID transactionLinkTypeId)
            throws MissingEntityException, TransactionLinkNotAllowedException, MissingTransactionDefinitionException {
        boolean allowed = false;

        // get the transactions
        Transaction fromTransaction = transactionService
                .getTransactionById(transactionLink.getFromTransactionId()).orElse(null);
        if (fromTransaction == null) {
            throw new MissingEntityException(transactionLink.getFromTransactionId());
        }
        Transaction toTransaction = transactionService
                .getTransactionById(transactionLink.getToTransactionId()).orElse(null);
        if (toTransaction == null) {
            throw new MissingEntityException(transactionLink.getToTransactionId());
        }
        // if the same definition o.k
        if (fromTransaction.getTransactionDefinitionKey().equals(toTransaction.getTransactionDefinitionKey())) {
            allowed = true;
        } else {
            // get definitions
            TransactionDefinition fromDefinition = transactionDefinitionService
                    .getTransactionDefinitionByKey(fromTransaction.getTransactionDefinitionKey())
                    .orElse(null);
            if (fromDefinition == null) {
                throw new MissingTransactionDefinitionException(fromTransaction.getTransactionDefinitionKey());
            }
            TransactionDefinition toDefinition = transactionDefinitionService
                    .getTransactionDefinitionByKey(toTransaction.getTransactionDefinitionKey())
                    .orElse(null);
            if (toDefinition == null) {
                throw new MissingTransactionDefinitionException(toTransaction.getTransactionDefinitionKey());
            }
            // compare their allowed links
            List<AllowedLink> fromAllowedLinks =
                    allowedLinkService.getAllowedLinksByDefinitionKey(fromDefinition.getKey());
            List<AllowedLink> toAllowedLinks =
                    allowedLinkService.getAllowedLinksByDefinitionKey(toDefinition.getKey());
            for (AllowedLink fromLink : fromAllowedLinks) {
                for (AllowedLink toLink : toAllowedLinks) {
                    if (fromLink.getTransactionLinkType().getId().equals(toLink.getTransactionLinkType().getId())) {
                        allowed = true;
                        break;
                    }
                }
            }
        }
        TransactionLinkType linkType = transactionLinkTypeService
                .getTransactionLinkTypeById(transactionLinkTypeId)
                .orElse(null);
        transactionLink.setTransactionLinkType(linkType);

        if (allowed) {
            return repository.save(transactionLink);
        } else {
            throw new TransactionLinkNotAllowedException(
                    transactionLink.getFromTransactionId(), transactionLink.getToTransactionId());
        }
    }

    /**
     * Get the linked transactions for a transaction id.
     *
     * @param id Transaction to get links for
     * @return List of linked transactions
     */
    public List<LinkedTransaction> getLinkedTransactionsById(UUID id) {
        List<LinkedTransaction> results = new ArrayList<>();
        List<TransactionLink> transactionLinks = repository.getTransactionLinksById(id);
        for (TransactionLink transactionLink : transactionLinks) {
            LinkedTransaction linkedTransaction = new LinkedTransaction();
            // create the association (from/to) and the linked transaction based on this transaction's id
            if (transactionLink.getFromTransactionId().equals(id)) {
                linkedTransaction.linkedTransactionId(transactionLink.getToTransactionId());
                linkedTransaction.setDescription(transactionLink.getTransactionLinkType().getFromDescription());
            } else if (transactionLink.getToTransactionId().equals(id)) {
                linkedTransaction.linkedTransactionId(transactionLink.getFromTransactionId());
                linkedTransaction.setDescription(transactionLink.getTransactionLinkType().getToDescription());
            }
            results.add(linkedTransaction);
        }
        return results;
    }
}
