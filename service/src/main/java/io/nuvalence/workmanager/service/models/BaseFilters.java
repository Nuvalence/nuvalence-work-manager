package io.nuvalence.workmanager.service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
abstract class BaseFilters {
    private String sortCol;
    private String sortDir;
    private Integer pageNumber;
    private Integer pageSize;
}
