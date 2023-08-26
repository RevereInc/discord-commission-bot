package com.flux.discordbot.discord.utility;

import net.dv8tion.jda.api.entities.Member;

import java.util.HashMap;
import java.util.Map;

public final class CommissionData {
    private static final Map<Member, String> selectedUserCategory = new HashMap<>();

    public static void setSelectedCategory(final Member member, final String category) {
        selectedUserCategory.put(member, category);
    }

    public static String getSelectedCategory(final Member member) {
        return selectedUserCategory.get(member);
    }

    public static void removeSelectedCategory(final Member member) {
        selectedUserCategory.remove(member);
    }
}
