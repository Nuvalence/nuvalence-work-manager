package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.transaction.AllowedLink;
import io.nuvalence.workmanager.service.generated.models.AllowedLinkCreationRequest;
import io.nuvalence.workmanager.service.generated.models.AllowedLinkModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Maps allowed links.
 *
 * <ul>
 *     <li>API Request Model ({@link io.nuvalence.workmanager.service.generated.models.AllowedLinkCreationRequest})</li>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.AllowedLinkModel})</li>
 *     <li>Logic/Persistence Model
 *     ({@link io.nuvalence.workmanager.service.domain.transaction.AllowedLink})</li>
 * </ul>
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AllowedLinkMapper {
    AllowedLinkMapper INSTANCE = Mappers.getMapper(AllowedLinkMapper.class);

    AllowedLinkModel allowedLinkToAllowedLinkModel(AllowedLink value);

    AllowedLink allowedLinkRequestToAllowedLink(AllowedLinkCreationRequest request);
}
