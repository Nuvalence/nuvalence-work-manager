package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.generated.models.TransactionDefinitionModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Maps transaction definitions between the following 2 forms.
 *
 * <ul>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.TransactionDefinitionModel})</li>
 *     <li>Logic/Persistence Model
 *     ({@link io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition})</li>
 * </ul>
 */
@Mapper
public interface TransactionDefinitionMapper {
    TransactionDefinitionMapper INSTANCE = Mappers.getMapper(TransactionDefinitionMapper.class);

    TransactionDefinitionModel transactionDefinitionToTransactionDefinitionModel(TransactionDefinition value);

    TransactionDefinition transactionDefinitionModelToTransactionDefinition(TransactionDefinitionModel model);
}
