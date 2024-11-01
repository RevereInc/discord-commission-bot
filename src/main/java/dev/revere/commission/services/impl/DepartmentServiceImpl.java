package dev.revere.commission.services.impl;

import dev.revere.commission.Constants;
import dev.revere.commission.entities.Department;
import dev.revere.commission.repository.DepartmentRepository;
import dev.revere.commission.services.DepartmentService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Implementation of the DepartmentService interface.
 * This class provides business logic for creating, updating, deleting, and retrieving departments.
 *
 * @author Remi
 * @project Commission-Discord-Bot-SpringBoot
 * @date 10/24/2024
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository m_departmentRepository;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository p_departmentRepository) {
        this.m_departmentRepository = p_departmentRepository;
    }

    @Override
    public void createDepartment(Department department) {
        m_departmentRepository.save(department);
    }

    /**
     * Retrieves the list of departments linked to the provided roles.
     *
     * @param departmentRole The selected role corresponding to departments.
     * @return List of Department entities that match the roles.
     */
    @Override
    public Department getDepartmentFromRole(Role departmentRole) {
        Department department = m_departmentRepository.findDepartmentByCommissionGuildRoleId(departmentRole.getIdLong());

        if (department == null) {
            department = m_departmentRepository.findDepartmentByMainGuildRoleId(departmentRole.getIdLong());
        }

        return department;
    }

    @Override
    public Department updateDepartment(String departmentName, Department updatedDepartment) {
        Department department = m_departmentRepository.findDepartmentByName(departmentName);
        if (department != null) {
            department.setName(updatedDepartment.getName());
            department.setMainGuildRoleId(updatedDepartment.getMainGuildRoleId());
            department.setCommissionGuildRoleId(updatedDepartment.getCommissionGuildRoleId());
            department.setMainGuildCategoryID(updatedDepartment.getMainGuildCategoryID());
            department.setCommissionGuildCategoryID(updatedDepartment.getCommissionGuildCategoryID());
            return m_departmentRepository.save(department);
        } else {
            throw new IllegalArgumentException("Department not found with name: " + departmentName);
        }
    }

    @Override
    public void createDepartmentInGuild(String departmentName, ShardManager p_shardManager) {
        Guild mainGuild = p_shardManager.getGuildById(Constants.MAIN_GUILD_ID);
        if (mainGuild == null) {
            throw new IllegalArgumentException("Main guild not found.");
        }

        mainGuild.createRole()
                .setName(departmentName)
                .setPermissions(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                .queue(mainRole -> {
                    Guild commissionGuild = mainGuild.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID);
                    if (commissionGuild == null) {
                        return;
                    }

                    commissionGuild.createRole()
                            .setName(departmentName)
                            .setPermissions(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL)
                            .queue(commissionRole -> {
                                mainGuild.createCategory(departmentName).queue(categoryInMainGuild -> {
                                    categoryInMainGuild.getManager()
                                            .putPermissionOverride(mainGuild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();

                                    commissionGuild.createCategory(departmentName).queue(categoryInCommissionGuild -> {
                                        categoryInCommissionGuild.getManager()
                                                .putPermissionOverride(commissionRole, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                                                .queue(success -> {
                                                    categoryInCommissionGuild.getManager()
                                                            .putPermissionOverride(commissionGuild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                                                            .queue();

                                                    Department department = new Department();
                                                    department.setName(departmentName);
                                                    department.setMainGuildRoleId(mainRole.getIdLong());
                                                    department.setCommissionGuildRoleId(commissionRole.getIdLong());
                                                    department.setMainGuildCategoryID(categoryInMainGuild.getIdLong());
                                                    department.setCommissionGuildCategoryID(categoryInCommissionGuild.getIdLong());

                                                    m_departmentRepository.save(department);
                                                }, failure -> {
                                                    throw new IllegalArgumentException("Failed to set permissions for commission guild category: " + failure.getMessage());
                                                });
                                    }, failure -> {
                                        throw new IllegalArgumentException("Failed to create category in commission guild: " + failure.getMessage());
                                    });
                                }, failure -> {
                                    throw new IllegalArgumentException("Failed to create category in main guild: " + failure.getMessage());
                                });
                            });
                });
    }

    @Override
    public void deleteDepartment(String departmentId) {
        m_departmentRepository.deleteById(departmentId);
    }

    @Override
    public void deleteDepartmentRoles(String departmentName, ShardManager p_shardManager) {
        Department department = m_departmentRepository.findDepartmentByName(departmentName);
        if (department == null) {
            throw new IllegalArgumentException("Department not found with name: " + departmentName);
        }

        Guild mainGuild = p_shardManager.getGuildById(Constants.MAIN_GUILD_ID);
        if (mainGuild != null) {
            Role mainRole = mainGuild.getRoleById(String.valueOf(department.getMainGuildRoleId()));
            if (mainRole != null) {
                mainRole.delete().queue();
            }
        }

        Guild commissionGuild = p_shardManager.getGuildById(Constants.COMMISSION_GUILD_ID);
        if (commissionGuild != null) {
            Role commissionRole = commissionGuild.getRoleById(String.valueOf(department.getCommissionGuildRoleId()));
            if (commissionRole != null) {
                commissionRole.delete().queue();
            }
        }
    }

    @Override
    public Department setMainGuildRoleId(String departmentName, long mainGuildRoleId) {
        Department department = m_departmentRepository.findDepartmentByName(departmentName);
        department.setMainGuildRoleId(mainGuildRoleId);
        return m_departmentRepository.save(department);
    }

    @Override
    public Department setCommissionGuildRoleId(String departmentName, long commissionGuildRoleId) {
        Department department = m_departmentRepository.findDepartmentByName(departmentName);
        department.setCommissionGuildRoleId(commissionGuildRoleId);
        return m_departmentRepository.save(department);
    }

    @Override
    public Department setMainGuildCategoryId(String departmentName, long categoryId) {
        Department department = m_departmentRepository.findDepartmentByName(departmentName);
        department.setMainGuildCategoryID(categoryId);
        return m_departmentRepository.save(department);
    }

    @Override
    public Department setCommissionGuildCategoryId(String departmentName, long categoryId) {
        Department department = m_departmentRepository.findDepartmentByName(departmentName);
        department.setCommissionGuildCategoryID(categoryId);
        return m_departmentRepository.save(department);
    }
}
