package ro.msg.learning.shop.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ro.msg.learning.shop.model.enums.UserRole;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String emailAddress;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}
