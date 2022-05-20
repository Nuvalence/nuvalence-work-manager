package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition;
import io.nuvalence.workmanager.service.repository.FormConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        return repository.save(formConfigDefinition);
    }

}
