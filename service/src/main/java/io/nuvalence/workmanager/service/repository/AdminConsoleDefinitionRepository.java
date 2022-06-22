package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.transaction.AdminConsoleDefinition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Admin Console Definitions.
 */
public interface AdminConsoleDefinitionRepository extends CrudRepository<AdminConsoleDefinition, UUID> {

    @Query("SELECT new AdminConsoleDefinition(ad.formId, ad.transactionDefinitionId, ad.formName, ad.description,  "
            + "ad.category, ad.version, ad.status, ad.createdTimeStamp, ad.createdBy, "
            + "ad.lastUpdatedBy, ad.translationRequired, ad.transactionDefinitionKey, ad.lastUpdatedTimeStamp) "
            + "FROM AdminConsoleDefinition ad")
    List<AdminConsoleDefinition> getAdminConsoleDashboard();
}
