package io.nuvalence.workmanager.service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.mapper.EntityMapper;
import io.nuvalence.workmanager.service.service.EntityService;
import io.nuvalence.workmanager.service.service.SchemaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser
class EntityApiDelegateImplTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EntityService entityService;

    @MockBean
    private SchemaService schemaService;

    @Test
    void getEntity() throws Exception {
        // Arrange
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final Entity entity = new Entity(schema, UUID.randomUUID());
        entity.set("attribute", "value");
        Mockito.when(entityService.getEntityById(entity.getId())).thenReturn(Optional.of(entity));

        // Act and Assert
        mockMvc.perform(get("/entity/" + entity.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entity.getId().toString()))
                .andExpect(jsonPath("$.schema").value("testschema"))
                .andExpect(jsonPath("$.data.attribute").value("value"));
    }

    @Test
    void getEntity404() throws Exception {
        // Arrange
        final UUID id = UUID.randomUUID();
        Mockito.when(entityService.getEntityById(id)).thenReturn(Optional.empty());

        // Act and Assert
        mockMvc.perform(get("/entity/" + id.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEntitiesBySchema() throws Exception {
        // Arrange
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final Entity entity1 = new Entity(schema, UUID.randomUUID());
        entity1.set("attribute", "value");
        final Entity entity2 = new Entity(schema, UUID.randomUUID());
        entity2.set("attribute", "othervalue");
        Mockito.when(entityService.getEntitiesBySchema(schema.getName())).thenReturn(List.of(entity1, entity2));

        // Act and Assert
        mockMvc.perform(get("/entity?schema=testschema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(entity1.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(entity2.getId().toString()));
    }

    @Test
    void postEntity() throws Exception {
        // Arrange
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final Entity entity = new Entity(schema, UUID.randomUUID());
        Mockito.when(schemaService.getSchemaByName(schema.getName())).thenReturn(Optional.of(schema));
        Mockito.when(entityService.saveEntity(entity)).thenReturn(entity);
        final String postBody = new ObjectMapper()
                .writeValueAsString(EntityMapper.INSTANCE.entityToEntityModel(entity));

        // Act and Assert
        mockMvc.perform(
                        post("/entity")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entity.getId().toString()));
    }

    @Test
    void postEntity424() throws Exception {
        // Arrange
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final Entity entity = new Entity(schema, UUID.randomUUID());
        Mockito.when(schemaService.getSchemaByName(schema.getName())).thenReturn(Optional.empty());
        final String postBody = new ObjectMapper()
                .writeValueAsString(EntityMapper.INSTANCE.entityToEntityModel(entity));

        // Act and Assert
        mockMvc.perform(
                        post("/entity")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isFailedDependency());
    }
}
