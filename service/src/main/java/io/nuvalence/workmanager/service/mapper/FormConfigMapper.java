package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition;
import io.nuvalence.workmanager.service.generated.models.FormConfigDefinitionModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Maps form configs between the following 2 forms.
 *
 * <ul>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.FormConfigDefinitionModel})</li>
 *     <li>Logic/Persistence Model
 *     ({@link io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition})</li>
 * </ul>
 */
@Mapper
public interface FormConfigMapper {
    FormConfigMapper INSTANCE = Mappers.getMapper(FormConfigMapper.class);

    FormConfigDefinitionModel formConfigToFormConfigModel(FormConfigDefinition value);

    FormConfigDefinition formConfigModelToFormConfig(FormConfigDefinitionModel model);
}
