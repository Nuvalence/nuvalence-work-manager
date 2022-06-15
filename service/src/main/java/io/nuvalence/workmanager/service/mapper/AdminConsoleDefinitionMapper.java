package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.transaction.AdminConsoleDefinition;
import io.nuvalence.workmanager.service.generated.models.AdminConsoleDashboardModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Maps transaction definitions between the following 2 forms.
 *
 * <ul>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.AdminConsoleDashboardModel})</li>
 *     <li>Logic/Persistence Model
 *     ({@link io.nuvalence.workmanager.service.domain.transaction.AdminConsoleDefinition})</li>
 * </ul>
 */
@Mapper
public interface AdminConsoleDefinitionMapper {

    AdminConsoleDefinitionMapper INSTANCE = Mappers.getMapper(AdminConsoleDefinitionMapper.class);

    AdminConsoleDashboardModel adminConsoleDefinitionToAdminConsoleDashboardModel(AdminConsoleDefinition value);

    AdminConsoleDefinition adminConsoleDashboardModelToAdminConsoleDefinition(AdminConsoleDashboardModel model);
}
