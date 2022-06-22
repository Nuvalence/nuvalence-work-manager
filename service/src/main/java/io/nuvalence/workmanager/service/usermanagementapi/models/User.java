package io.nuvalence.workmanager.service.usermanagementapi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@Jacksonized
public class User {
    private UUID id;

    private String displayName;

    private String email;

    private String externalId;

    private List<Role> assignedRoles;
}
