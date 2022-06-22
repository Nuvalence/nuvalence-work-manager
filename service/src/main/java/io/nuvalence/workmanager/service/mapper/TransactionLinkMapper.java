package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.transaction.TransactionLink;
import io.nuvalence.workmanager.service.generated.models.TransactionLinkCreationRequest;
import io.nuvalence.workmanager.service.generated.models.TransactionLinkModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Maps transaction links.
 *
 * <ul>
 *     <li>API Request ({@link io.nuvalence.workmanager.service.generated.models.TransactionLinkCreationRequest})</li>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.TransactionLinkModel})</li>
 *     <li>Logic/Persistence Model
 *     ({@link io.nuvalence.workmanager.service.domain.transaction.TransactionLink})</li>
 * </ul>
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionLinkMapper {
    TransactionLinkMapper INSTANCE = Mappers.getMapper(TransactionLinkMapper.class);

    TransactionLinkModel transactionLinkToTransactionLinkModel(TransactionLink value);

    TransactionLink transactionLinkRequestToTransactionLink(TransactionLinkCreationRequest request);
}