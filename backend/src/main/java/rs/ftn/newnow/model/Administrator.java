package rs.ftn.newnow.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import rs.ftn.newnow.model.enums.Role;

@Entity
@Table(name = "administrators")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Administrator extends User {

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        getRoles().add(Role.ROLE_ADMIN);
    }
}
