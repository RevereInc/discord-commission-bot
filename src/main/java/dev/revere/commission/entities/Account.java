package dev.revere.commission.entities;

import dev.revere.commission.services.AuthService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/25/2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class Account {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    @Field("role")
    private AuthService.Role role;
}
