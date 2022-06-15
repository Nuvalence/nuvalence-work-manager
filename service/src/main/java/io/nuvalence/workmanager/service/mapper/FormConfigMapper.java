package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition;
import io.nuvalence.workmanager.service.generated.models.FormConfigDefinitionModel;
import io.nuvalence.workmanager.service.generated.models.FormConfigDefinitionModel.StatusEnum;
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

    /**
     * Use generated String -> Enum mapper from FormConfigDefinitionModel.
     * @param status Status encoded as string.
     * @return Status enum.
     */
    default StatusEnum toStatusEnum(String status) {
        return status != null ? StatusEnum.fromValue(status) : null;
    }

    /**
     * Use generated Enum -> String mapper from FormConfigDefinitionModel.
     * @param status Status enum.
     * @return Status encoded as string.
     */
    default String toStatusString(StatusEnum status) {
        return status != null ? status.getValue() : null;
    }
}
