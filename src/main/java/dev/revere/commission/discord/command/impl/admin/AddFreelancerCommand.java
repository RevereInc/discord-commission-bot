package dev.revere.commission.discord.command.impl.admin;

import com.stripe.model.AccountLink;
import com.stripe.param.AccountLinkCreateParams;
import dev.revere.commission.Constants;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.repository.FreelancerRepository;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.revere.commission.services.DepartmentService;
import dev.revere.commission.services.FreelancerService;
import dev.revere.commission.services.PaymentService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@Service
public class AddFreelancerCommand extends SlashCommand {
    private static final Logger log = LoggerFactory.getLogger(AddFreelancerCommand.class);
    @Value("${stripe.api-key}")
    private String apiKey;

    private final FreelancerRepository m_freelancerRepository;
    private final FreelancerService m_freelancerService;
    private final DepartmentService m_departmentService;
    private final PaymentService m_paymentService;

    @Autowired
    public AddFreelancerCommand(final FreelancerRepository p_freelancerRepository, final FreelancerService p_freelancerService, final DepartmentService p_departmentService, PaymentService p_paymentService) {
        m_freelancerRepository = p_freelancerRepository;
        m_freelancerService = p_freelancerService;
        m_departmentService = p_departmentService;
        m_paymentService = p_paymentService;

        this.name = "addfreelancer";
        this.help = "Add a freelancer to the database";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        final List<OptionData> optionData = new ArrayList<>();
        optionData.add(new OptionData(OptionType.USER, "user", "the person to become a freelancer").setRequired(true));
        optionData.add(new OptionData(OptionType.ROLE, "department", "the department to put the person in").setRequired(true));

        this.options = optionData;
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final Member member = Objects.requireNonNull(p_slashCommandEvent.getOption("user")).getAsMember();
        final Role departmentRole = Objects.requireNonNull(p_slashCommandEvent.getOption("department")).getAsRole();

        if (member == null) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("User not found")).setEphemeral(true).queue();
            return;
        }

        final Freelancer existingFreelancer = m_freelancerRepository.findFreelancerByUserId(member.getUser().getIdLong());
        final Department department = m_departmentService.getDepartmentFromRole(departmentRole);

        if (department == null) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Department not found")).setEphemeral(true).queue();
            return;
        }

        if (existingFreelancer != null) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("A freelancer with the name of, ``" + name + "`` already exists.")).setEphemeral(false).queue();
            return;
        }

        Guild mainGuild = p_slashCommandEvent.getJDA().getGuildById(Constants.MAIN_GUILD_ID);
        Guild commissionGuild = p_slashCommandEvent.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID);
        if (mainGuild == null || commissionGuild == null) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Guild not found")).setEphemeral(true).queue();
            return;
        }

        if (commissionGuild.getMember(member.getUser()) == null) {
            commissionGuild.getTextChannelById(Constants.FREELANCING_BASICS_CHANNEL_ID).createInvite().setMaxAge(43200)
                .setMaxUses(1)
                .queue(invite -> {
                    String inviteLink = invite.getUrl();
                    member.getUser().openPrivateChannel().queue(privateChannel -> {
                        String description = String.format(
                                """
                                        You have been invited to join **Tonic Consulting** as a freelancer.
                                        Please click the link below to join the server.
                                        ### <:1270446417206312980:1299806078044868719> Invite:
                                        - [Click here to join the server](%s)
                                        """,
                                inviteLink
                        );
                        privateChannel.sendMessage(TonicEmbedBuilder.sharedMessageEmbed(description)).queue();
                    }, throwable -> {
                        p_slashCommandEvent.getChannel().sendMessage(member.getAsMention() + " You need to join the commission guild to be added as a freelancer. Please contact support for an invite.").queue();
                        p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("User is not in the commission guild, and I couldn't DM them.")).setEphemeral(true).queue();
                    });
                }, throwable -> {
                    p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Could not create an invite link. Please try again later.")).setEphemeral(true).queue();
                });
        }

        final Freelancer freelancer = new Freelancer();
        freelancer.setName(member.getUser().getName());
        freelancer.setUserId(member.getUser().getIdLong());
        freelancer.setPortfolio("");

        Role mainRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(department.getMainGuildRoleId()));
        Role commissionRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(department.getCommissionGuildRoleId()));
        Role globalRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(Constants.GLOBAL_FREELANCER_ROLE_ID));

        mainGuild.addRoleToMember(member, globalRole).queue();
        mainGuild.addRoleToMember(member, mainRole).queue();

        if (commissionGuild.getMember(member.getUser()) != null) {
            commissionGuild.addRoleToMember(member, commissionRole).queue();
        }

        String[] accountDetails = m_paymentService.createStripeAccount(member.getUser().getName());
        if (accountDetails != null) {
            String stripeAccountId = accountDetails[0];
            String onboardingUrl = accountDetails[1];

            freelancer.setStripeAccountId(stripeAccountId);

            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Please complete your onboarding process: " + onboardingUrl)).setEphemeral(true).queue();

            m_freelancerService.addDepartment(freelancer, department);
            m_freelancerRepository.save(freelancer);

            p_slashCommandEvent.reply(createdFreelancer(freelancer.getName())).setEphemeral(false).queue();
        } else {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Failed to create Stripe account for freelancer.")).setEphemeral(true).queue();
        }
    }

    /**
     * Create and configure the created freelancer message.
     *
     * @return MessageCreateData containing the support embed.
     */
    public MessageCreateData createdFreelancer(String name) {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(name + " has successfully been added as a freelancer")
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }
}
