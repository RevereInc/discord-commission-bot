package dev.revere.commission.repository;

import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.entities.Review;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository interface for managing Department entities in MongoDB.
 * This interface provides methods for CRUD operations on Department objects.
 *
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
public interface DepartmentRepository extends MongoRepository<Department, String> {

    /**
     * Find a department by the department name.
     *
     * @param departmentName The name of the department.
     * @return The Department object associated with the given department name.
     */
    Department findDepartmentByName(String departmentName);

    /**
     * Find a department by the main guild role ID.
     *
     * @param mainGuildRoleId The ID of the main guild role associated with the department.
     * @return The Department object associated with the given main guild role ID.
     */
    Department findDepartmentByMainGuildRoleId(long mainGuildRoleId);

    /**
     * Find a department by the commission guild role ID.
     *
     * @param commissionGuildRoleId The ID of the commission guild role associated with the department.
     * @return The Department object associated with the given commission guild role ID.
     */
    Department findDepartmentByCommissionGuildRoleId(long commissionGuildRoleId);

    /**
     * Check if a department with the specified name exists in the repository.
     *
     * @param departmentName The name of the department.
     * @return `true` if a department with the given name exists, `false` otherwise.
     */
    boolean existsDepartmentByName(String departmentName);

    /**
     * Retrieve the total amount of all departments
     *
     * @return The count of all departments
     */
    @NotNull
    List<Department> findAll();
}