package com.flux.discordbot.discord.command.impl;

import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.entities.TitleDescription;
import com.flux.discordbot.repository.FreelancerRepository;
import com.flux.discordbot.services.ReviewService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

@Service
public class InfoCommand extends SlashCommand {
    private final FreelancerRepository m_freelancerRepository;
    private final ReviewService m_reviewService;

    @Autowired
    public InfoCommand(final FreelancerRepository p_freelancerRepository, final ReviewService p_reviewService) {
        m_freelancerRepository = p_freelancerRepository;
        m_reviewService = p_reviewService;

        this.name = "info";
        this.help = "Freelancer info";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        // Define command options

        this.options = Collections.singletonList(new OptionData(OptionType.USER, "user", "userid of the freelancer").setRequired(true));
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final User userId = p_slashCommandEvent.getOption("user").getAsUser();

        if (!m_freelancerRepository.existsFreelancerByUserId(userId.getIdLong())) {
            p_slashCommandEvent.reply("Could not find freelancer with name " + userId.getName()).queue();
            return;
        }

        final Freelancer freelancer = m_freelancerRepository.findFreelancerByUserId(userId.getIdLong());

        final String name = freelancer.getName();

        final float rating = m_reviewService.averageRating(freelancer); // TODO: Create star emoji representation

        final List<Role> serviceRoles = freelancer.getServiceRoleIds()
                .stream()
                .map(p_roleId -> p_slashCommandEvent.getJDA().getRoleById(p_roleId))
                .toList();

        final StringJoiner stringJoiner = new StringJoiner(", ");

        serviceRoles.stream()
                .map(IMentionable::getAsMention)
                .forEach(stringJoiner::add);

        final String serviceRolesPretty = stringJoiner.toString();

        final String bio = freelancer.getBio();

        final List<TitleDescription> titleDescriptionCards = freelancer.getTitleDescriptions();

        p_slashCommandEvent.reply(
                "**NAME** " + name + "\n"
                + "**AVG RATING** " + rating + "\n"
                + "**SERVICES** " + serviceRolesPretty + "\n"
                + "** BIO ** " + bio + "\n"
                + "**CARDS** i need an embed to display these, too lazy"
        ).queue();
    }
}
