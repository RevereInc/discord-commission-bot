package dev.revere.commission.discord.command.impl;

import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.entities.TitleDescription;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.services.ReviewService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

@Service
public class ProfileCommand extends SlashCommand {
    private final FreelancerRepository m_freelancerRepository;
    private final ReviewService m_reviewService;

    @Autowired
    public ProfileCommand(final FreelancerRepository p_freelancerRepository, final ReviewService p_reviewService) {
        m_freelancerRepository = p_freelancerRepository;
        m_reviewService = p_reviewService;

        this.name = "profile";
        this.help = "View a freelancer's profile";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        // Define command options
        this.options = Collections.singletonList(new OptionData(OptionType.USER, "user", "userid of the freelancer").setRequired(false));
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final User user = p_slashCommandEvent.getOption("user") == null
                ? p_slashCommandEvent.getUser()
                : p_slashCommandEvent.getOption("user").getAsUser();

        if (!m_freelancerRepository.existsFreelancerByUserId(user.getIdLong())) {
            if (user == p_slashCommandEvent.getUser()) {
                p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You are not currently a freelancer! Feel free to apply.")).setEphemeral(true).queue();
                return;
            }
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Could not find freelancer with name " + user.getName())).queue();
            return;
        }

        final Freelancer freelancer = m_freelancerRepository.findFreelancerByUserId(user.getIdLong());

        final String name = freelancer.getName();

        final float rating = m_reviewService.averageRating(freelancer);
        final String prettyRating = m_reviewService.parseRating(rating);

        final List<Department> departments = freelancer.getDepartments();
        final StringJoiner departmentNames = new StringJoiner(", ");
        departments.stream()
                .map(Department::getName)
                .forEach(departmentNames::add);

        final String departmentNamesPretty = departmentNames.toString();

        final String portfolio = freelancer.getPortfolio().isEmpty() ? "No portfolio" : freelancer.getPortfolio();

        final List<TitleDescription> titleDescriptionCards = freelancer.getTitleDescriptions();

        final String iconUrl = user.getAvatarUrl();

        p_slashCommandEvent.reply(informationEmbed(name, prettyRating, departmentNamesPretty, portfolio, titleDescriptionCards, iconUrl)).queue();
    }

    public MessageCreateData informationEmbed(final String p_member, final String p_rating, final String p_departmentNamesPretty,
                                              final String p_portfolio, final List<TitleDescription> p_titleDescriptions, final String p_iconUrl) {
        TonicEmbedBuilder tonicEmbedBuilder = new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription("View **" + p_member + "'s** freelancer statistics")
                .addField("Departments", p_departmentNamesPretty, false)
                .addField("Rating", String.valueOf(p_rating), false)
                .addField("Portfolio", p_portfolio, false)
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .setThumbnail(p_iconUrl);

        if (p_titleDescriptions != null && !p_titleDescriptions.isEmpty()) {
            for (final TitleDescription titleDescription : p_titleDescriptions) {
                tonicEmbedBuilder = tonicEmbedBuilder.addField(
                        titleDescription.getTitle(),
                        titleDescription.getDescription(),
                        true);
            }
        }

        return tonicEmbedBuilder.build();
    }
}
