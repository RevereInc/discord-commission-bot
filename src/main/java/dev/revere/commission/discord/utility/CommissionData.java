package dev.revere.commission.discord.utility;

import dev.revere.commission.entities.Department;
import net.dv8tion.jda.api.entities.Member;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
public final class CommissionData {
    private static final Map<Member, Department> selectedUserCategory = new HashMap<>();

    public static void setSelectedCategory(final Member member, final Department category) {
        selectedUserCategory.put(member, category);
    }

    public static Department getSelectedCategory(final Member member) {
        return selectedUserCategory.get(member);
    }

    public static void removeSelectedCategory(final Member member) {
        selectedUserCategory.remove(member);
    }
}
