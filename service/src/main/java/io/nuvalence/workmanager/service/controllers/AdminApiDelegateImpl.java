package io.nuvalence.workmanager.service.controllers;

import io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.generated.controllers.AdminApiDelegate;
import io.nuvalence.workmanager.service.generated.models.FormConfigDefinitionModel;
import io.nuvalence.workmanager.service.generated.models.SchemaModel;
import io.nuvalence.workmanager.service.generated.models.TransactionDefinitionModel;
import io.nuvalence.workmanager.service.mapper.FormConfigMapper;
import io.nuvalence.workmanager.service.mapper.SchemaMapper;
import io.nuvalence.workmanager.service.mapper.TransactionDefinitionMapper;
import io.nuvalence.workmanager.service.service.FormConfigService;
import io.nuvalence.workmanager.service.service.SchemaService;
import io.nuvalence.workmanager.service.service.TransactionDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller layer for API.
 */
@Service
@RequiredArgsConstructor
public class AdminApiDelegateImpl implements AdminApiDelegate {
    private final FormConfigService formConfigService;
    private final SchemaService schemaService;
    private final TransactionDefinitionService transactionDefinitionService;

    @Override
    public ResponseEntity<SchemaModel> getSchema(String name) {
        final Optional<SchemaModel> schema = schemaService.getSchemaByName(name)
                .map(SchemaMapper.INSTANCE::schemaToSchemaModel);

        return (schema.isEmpty())
                ? ResponseEntity.status(404).build()
                : ResponseEntity.status(200).body(schema.get());
    }

    @Override
    public ResponseEntity<List<SchemaModel>> getSchemas(final String search) {
        final List<SchemaModel> results = schemaService.getSchemasByPartialNameMatch(search).stream()
                .map(SchemaMapper.INSTANCE::schemaToSchemaModel)
                .collect(Collectors.toList());

        return ResponseEntity.status(200).body(results);
    }

    @Override
    public ResponseEntity<Void> postSchema(SchemaModel schemaModel) {
        schemaService.saveSchema(SchemaMapper.INSTANCE.schemaModelToSchema(schemaModel));

        return ResponseEntity.status(204).build();
    }

    @Transactional
    @Override
    public ResponseEntity<TransactionDefinitionModel> getTransactionDefinition(UUID id) {
        final Optional<TransactionDefinitionModel> transactionDefinition = transactionDefinitionService
                .getTransactionDefinitionById(id)
                .map(TransactionDefinitionMapper.INSTANCE::transactionDefinitionToTransactionDefinitionModel);

        return (transactionDefinition.isEmpty())
                ? ResponseEntity.status(404).build()
                : ResponseEntity.status(200).body(transactionDefinition.get());
    }

    @Transactional
    @Override
    public ResponseEntity<List<TransactionDefinitionModel>> getTransactionDefinitions(String name) {
        final List<TransactionDefinitionModel> results = transactionDefinitionService
                .getTransactionDefinitionsByPartialNameMatch(name).stream()
                .map(TransactionDefinitionMapper.INSTANCE::transactionDefinitionToTransactionDefinitionModel)
                .collect(Collectors.toList());

        return ResponseEntity.status(200).body(results);
    }

    @Override
    public ResponseEntity<List<TransactionDefinitionModel>> getTransactionDefinitionsByCategory(String category) {
        final List<TransactionDefinitionModel> results = transactionDefinitionService
                .getTransactionDefinitionsByPartialCategoryMatch(category).stream()
                .map(TransactionDefinitionMapper.INSTANCE::transactionDefinitionToTransactionDefinitionModel)
                .collect(Collectors.toList());

        return ResponseEntity.status(200).body(results);
    }

    @Override
    public ResponseEntity<TransactionDefinitionModel> postTransactionDefinition(
            TransactionDefinitionModel transactionDefinitionModel) {
        final TransactionDefinition transactionDefinition = transactionDefinitionService.saveTransactionDefinition(
                TransactionDefinitionMapper.INSTANCE
                        .transactionDefinitionModelToTransactionDefinition(transactionDefinitionModel)
        );

        return ResponseEntity
                .status(200)
                .body(
                        TransactionDefinitionMapper.INSTANCE
                                .transactionDefinitionToTransactionDefinitionModel(transactionDefinition)
                );
    }

    @Override
    public ResponseEntity<FormConfigDefinitionModel> getFormConfigById(UUID id) {
        final Optional<FormConfigDefinitionModel> formConfigDefinition = formConfigService
                .getFormConfigDefinitionById(id)
                .map(FormConfigMapper.INSTANCE::formConfigToFormConfigModel);

        return (formConfigDefinition.isEmpty())
                ? ResponseEntity.status(404).build()
                : ResponseEntity.status(200).body(formConfigDefinition.get());
    }

    @Override
    public ResponseEntity<FormConfigDefinitionModel> getFormConfigByName(String name) {
        final Optional<FormConfigDefinitionModel> formConfigDefinition = formConfigService
                .getFormConfigDefinitionByName(name)
                .map(FormConfigMapper.INSTANCE::formConfigToFormConfigModel);

        return (formConfigDefinition.isEmpty())
                ? ResponseEntity.status(404).build()
                : ResponseEntity.status(200).body(formConfigDefinition.get());
    }

    @Override
    public ResponseEntity<List<FormConfigDefinitionModel>> getFormConfigs(String search) {
        final List<FormConfigDefinitionModel> results = formConfigService
                .getFormConfigDefinitionsByPartialNameMatch(search).stream()
                .map(FormConfigMapper.INSTANCE::formConfigToFormConfigModel)
                .collect(Collectors.toList());

        return ResponseEntity.status(200).body(results);
    }

    @Override
    public ResponseEntity<FormConfigDefinitionModel> postFormConfig(
            FormConfigDefinitionModel formConfigDefinitionModel) {
        final FormConfigDefinition formConfigDefinition = formConfigService.saveFormConfigDefinition(
                FormConfigMapper.INSTANCE.formConfigModelToFormConfig(formConfigDefinitionModel)
        );

        return ResponseEntity
                .status(200)
                .body(
                        FormConfigMapper.INSTANCE.formConfigToFormConfigModel(formConfigDefinition)
                );
    }
}
