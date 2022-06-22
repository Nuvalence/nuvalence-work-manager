package io.nuvalence.workmanager.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition;
import io.nuvalence.workmanager.service.generated.models.FormConfigDefinitionModel;
import io.nuvalence.workmanager.service.repository.FormConfigRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

/**
 * Service layer to manage form config definitions.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class FormConfigService {
    private final FormConfigRepository repository;

    /**
     * Fetches a form config from the database by id (primary key).
     *
     * @param id form config id to fetch
     * @return form config object
     */
    public Optional<FormConfigDefinition> getFormConfigDefinitionById(final UUID id) {
        return repository.findById(id);
    }

    /**
     * Fetches a form config from the database by name.
     *
     * @param name form config name to fetch
     * @return form config object
     */
    public Optional<FormConfigDefinition> getFormConfigDefinitionByName(final String name) {
        return repository.searchByName(name);
    }

    /**
     * Returns a list of form config definitions whose names match the query passed in.
     *
     * @param query Partial name query
     * @return List of form config definitions matching query
     */
    public List<FormConfigDefinition> getFormConfigDefinitionsByPartialNameMatch(final String query) {
        return repository.searchByPartialName((query == null) ? "" : query);
    }

    /**
     * Saves a transaction definition.
     *
     * @param formConfigDefinition form config definition to save.
     * @return post0save version of form config definition.
     */
    public FormConfigDefinition saveFormConfigDefinition(final FormConfigDefinition formConfigDefinition) {

        Logger logger = null;
        if (formConfigDefinition.getRendererOptions().get("i18n") != null) {
            try {
                String i18nString = "";
                ObjectMapper mapper = new ObjectMapper();
                i18nString = mapper.writeValueAsString(formConfigDefinition.getRendererOptions().get("i18n"));
                JsonNode i18nObject = mapper.readTree(i18nString);

                String[] acceptedLanguages = {"es", "en", "ar", "be", "ch", "ha", "it", "ko", "po", "ru", "yi"};
                boolean translationMissing = false;
                for (String key: acceptedLanguages) {
                    if (!i18nObject.has(key)) {
                        translationMissing = true;
                    }
                }
                formConfigDefinition.setTranslationRequired(translationMissing);
            } catch (JsonProcessingException e) {
                assert false;
                logger.error(e);
            }
        } else {
            formConfigDefinition.setTranslationRequired(true);
        }

        if (formConfigDefinition.getCreatedTimeStamp() == null
            || formConfigDefinition.getLastUpdatedTimeStamp() == null) {

            formConfigDefinition.setCreatedTimeStamp(OffsetDateTime.now());
            formConfigDefinition.setLastUpdatedTimeStamp(OffsetDateTime.now());
        } else {
            formConfigDefinition.setLastUpdatedTimeStamp(OffsetDateTime.now());
        }
        return repository.save(formConfigDefinition);
    }

    /**
     * Updates the status of a form and returns the modified form.
     * @param id form configuration ID.
     * @param status updated status.
     * @return modified form.
     */
    private Optional<FormConfigDefinition> updateFormStatus(final UUID id, String status) {
        Optional<FormConfigDefinition> formConfigDefinition = this.getFormConfigDefinitionById(id);

        if (formConfigDefinition.isEmpty()) {
            return Optional.empty();
        }

        FormConfigDefinition formConfig = formConfigDefinition.get();
        formConfig.setStatus(status);

        return Optional.of(repository.save(formConfig));
    }

    /**
     * Updates the status of a form configuration to published.
     * @param id form config ID
     * @return the updated form.
     */
    public Optional<FormConfigDefinition> publishFormConfig(final UUID id) {
        final Optional<FormConfigDefinition> formConfig = this.updateFormStatus(id,
                FormConfigDefinitionModel.StatusEnum.PUBLISHED.getValue()
        );

        if (formConfig.isEmpty()) {
            return Optional.empty();
        }

        return formConfig;
    }

    /**
     * Updates the status of a form configuration to draft.
     * @param id form config ID.
     * @return the updated form.
     */
    public Optional<FormConfigDefinition> unpublishFormConfig(final UUID id) {
        final Optional<FormConfigDefinition> formConfig = this.updateFormStatus(id,
                FormConfigDefinitionModel.StatusEnum.DRAFT.getValue()
        );

        if (formConfig.isEmpty()) {
            return Optional.empty();
        }

        return formConfig;
    }

}
