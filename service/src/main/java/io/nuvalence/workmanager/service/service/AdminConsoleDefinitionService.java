package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.transaction.AdminConsoleDefinition;

import io.nuvalence.workmanager.service.repository.AdminConsoleDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import javax.transaction.Transactional;

/**
 * Service layer to manage transaction definitions.
 */

@Component
@Transactional
@RequiredArgsConstructor
public class AdminConsoleDefinitionService {

    private final AdminConsoleDefinitionRepository repository;

    public List<AdminConsoleDefinition> getAdminConsoleDashboardService() {
        return repository.getAdminConsoleDashboard();
    }
}
