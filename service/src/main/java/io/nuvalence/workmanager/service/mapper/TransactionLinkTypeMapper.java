package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.transaction.TransactionLinkType;
import io.nuvalence.workmanager.service.generated.models.TransactionLinkTypeModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Maps transaction link types.
 *
 * <ul>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.TransactionLinkTypeModel})</li>
 *     <li>Logic/Persistence Model
 *     ({@link io.nuvalence.workmanager.service.domain.transaction.TransactionLinkType})</li>
 * </ul>
 */
@Mapper
public interface TransactionLinkTypeMapper {
    TransactionLinkTypeMapper INSTANCE = Mappers.getMapper(TransactionLinkTypeMapper.class);

    TransactionLinkTypeModel transactionLinkTypeToTransactionLinkTypeModel(TransactionLinkType value);

    TransactionLinkType transactionLinkTypeModelToTransactionLinkType(TransactionLinkTypeModel model);
}