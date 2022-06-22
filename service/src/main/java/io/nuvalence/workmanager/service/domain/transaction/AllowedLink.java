package io.nuvalence.workmanager.service.domain.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Link type that is allowed for a transaction definition.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "allowed_link")
public class AllowedLink {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID id;

    @Column(name = "transaction_definition_key")
    private String transactionDefinitionKey;

    @OneToOne
    @JoinColumn(name = "transaction_link_type_id", nullable = false)
    @Builder.Default
    private TransactionLinkType transactionLinkType = null;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AllowedLink that = (AllowedLink) o;
        return Objects.equals(id, that.id)
                && Objects.equals(transactionDefinitionKey, that.transactionDefinitionKey)
                && Objects.equals(transactionLinkType, that.transactionLinkType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionDefinitionKey, transactionLinkType);
    }
}
