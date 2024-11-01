package dev.revere.commission.services;

import dev.revere.commission.entities.Department;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * Service interface for managing departments.
 * This interface provides methods for creating, updating, deleting, and retrieving departments.
 *
 * @author Remi
 * @project Commission-Discord-Bot-SpringBoot
 * @date 10/24/2024
 */
public interface DepartmentService {
    /**
     * Create a new department.
     *
     * @param department The Department object to create.
     */
    void createDepartment(Department department);

    /**
     * Create a new department in a guild.
     *
     * @param p_departmentName The name of the department to create.
     * @param p_shardManager The JDA object for the guild.
     */
    void createDepartmentInGuild(String p_departmentName, ShardManager p_shardManager);

    /**
     * Delete a department by the department name.
     *
     * @param departmentName The name of the department to delete.
     */
    void deleteDepartment(String departmentName);

    /**
     * Delete the roles and categories associated with a department.
     *
     * @param p_departmentName The name of the department to delete.
     * @param p_shardManager The JDA object for the guild.
     */
    void deleteDepartmentRoles(String p_departmentName, ShardManager p_shardManager);

    /**
     * Update an existing department.
     *
     * @param departmentName The name of the department to update.
     * @param updatedDepartment The updated Department object.
     * @return The Department object that was updated.
     */
    Department updateDepartment(String departmentName, Department updatedDepartment);

    /**
     * Retrieves the department linked to the provided role.
     *
     * @param departmentRole The selected role corresponding to a department.
     * @return The Department entity that matches the role.
     */
    Department getDepartmentFromRole(Role departmentRole);

    /**
     * Set the main guild role ID for a department.
     *
     * @param departmentName The name of the department to update.
     * @param mainGuildRoleId The ID of the main guild role to set.
     * @return The Department object that was updated.
     */
    Department setMainGuildRoleId(String departmentName, long mainGuildRoleId);

    /**
     * Set the commission guild role ID for a department.
     *
     * @param departmentName The name of the department to update.
     * @param commissionGuildRoleId The ID of the commission guild role to set.
     * @return The Department object that was updated.
     */
    Department setCommissionGuildRoleId(String departmentName, long commissionGuildRoleId);

    /**
     * Set the category ID for a department.
     *
     * @param departmentName The name of the department to update.
     * @param categoryId The ID of the category to set.
     * @return The Department object that was updated.
     */
    Department setMainGuildCategoryId(String departmentName, long categoryId);

    /**
     * Set the commission guild category ID for a department.
     *
     * @param departmentName The name of the department to update.
     * @param categoryId The ID of the category to set.
     * @return The Department object that was updated.
     */
    Department setCommissionGuildCategoryId(String departmentName, long categoryId);
}
