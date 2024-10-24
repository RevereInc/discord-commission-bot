package dev.revere.commission.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/24/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "departments")
public class Department {
    @Id
    private String id;

    // The name of the department
    private String name;

    // The role id of the department in the main guild
    private long mainGuildRoleId;

    // The role id of the department in the commission guild
    private long commissionGuildRoleId;

    // The category id of the department in the main guild
    private long mainGuildCategoryID;

    // The category id of the department in the commission guild
    private long commissionGuildCategoryID;
}
