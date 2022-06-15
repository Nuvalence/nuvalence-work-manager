package io.nuvalence.workmanager.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaRow;
import io.nuvalence.workmanager.service.mapper.SchemaMapper;
import io.nuvalence.workmanager.service.repository.SchemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

/**
 * Service layer to manage Schemas.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class SchemaService {
    private final SchemaRepository schemaRepository;

    /**
     * Fetches a schema from the database by name (primary key).
     *
     * @param name Schema name to fetch
     * @return Schema object
     */
    public Optional<Schema> getSchemaByName(final String name) {
        return schemaRepository.findById(name).map(row -> {
            try {
                return SchemaMapper.INSTANCE.schemaRowToSchema(row);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to parse schema JSON stored in database.", e);
            }
        });
    }

    /**
     * Returns a list of schemas whose names match the query passed in.
     *
     * @param query Partial name query
     * @return List of Schemas matching query
     */
    public List<Schema> getSchemasByPartialNameMatch(final String query) {
        List<SchemaRow> schemaList;
        if (query == null) {
            schemaList = schemaRepository.getAllSchemas();
        } else {
            schemaList = schemaRepository.searchByPartialName(query);
        }

        return schemaList.stream()
                .map((row) -> {
                    try {
                        return SchemaMapper.INSTANCE.schemaRowToSchema(row);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Unable to parse schema JSON stored in database.", e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Saves a schema.
     *
     * @param schema Schema to save.
     */
    public void saveSchema(final Schema schema) {
        try {
            schemaRepository.save(SchemaMapper.INSTANCE.schemaToSchemaRow(schema));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to marshall schema to JSON.", e);
        }
    }
}
