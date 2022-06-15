package io.nuvalence.workmanager.service.usermanagementapi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.UUID;

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Role {
    private UUID id;

    private String roleName;

    private ArrayList permissions;
}
