package dev.revere.commission.discord.command.impl.admin;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.services.FreelancerService;
import dev.revere.commission.services.PaymentService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 11/1/2024
 */
@Service
public class CreateStripeAccountCommand extends SlashCommand {
    private static final Logger log = LoggerFactory.getLogger(CreateStripeAccountCommand.class);

    private final FreelancerRepository m_freelancerRepository;
    private final PaymentService m_paymentService;

    @Value("${stripe.api-key}")
    private String apiKey;

    @Autowired
    public CreateStripeAccountCommand(final FreelancerRepository p_freelancerRepository, final PaymentService p_paymentService) {
        m_freelancerRepository = p_freelancerRepository;
        m_paymentService = p_paymentService;

        this.name = "createstripecount";
        this.help = "Create a Stripe account for old freelancers who weren't created after the cutoff date.";
        this.guildOnly = true;

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        final List<OptionData> optionData = new ArrayList<>();
        optionData.add(new OptionData(OptionType.STRING, "countrycode", "the country code for the freelancer").setRequired(true));
        optionData.add(new OptionData(OptionType.USER, "user", "the freelancer to create a Stripe account for").setRequired(true));
        this.options = optionData;
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final Member member = Objects.requireNonNull(p_slashCommandEvent.getOption("user")).getAsMember();
        final String countryCode = Objects.requireNonNull(p_slashCommandEvent.getOption("countrycode")).getAsString();

        if (member == null) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("User not found")).setEphemeral(true).queue();
            return;
        }

        final Freelancer freelancer = m_freelancerRepository.findFreelancerByUserId(member.getUser().getIdLong());

        if (freelancer == null) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("No freelancer found for this user.")).setEphemeral(true).queue();
            return;
        }

        if (freelancer.getStripeAccountId() != null && !freelancer.getStripeAccountId().isEmpty()) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Freelancer already has a Stripe account.")).setEphemeral(true).queue();
            return;
        }

        String[] accountDetails = m_paymentService.createStripeAccount(countryCode);
        if (accountDetails != null) {
            String stripeAccountId = accountDetails[0];
            String onboardingUrl = accountDetails[1];

            freelancer.setStripeAccountId(stripeAccountId);
            m_freelancerRepository.save(freelancer);

            member.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Please complete your onboarding process: " + onboardingUrl)).queue());
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Please complete your onboarding process: " + onboardingUrl)).setEphemeral(true).queue();
        } else {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Failed to create Stripe account for freelancer.")).setEphemeral(true).queue();
        }
    }
}
