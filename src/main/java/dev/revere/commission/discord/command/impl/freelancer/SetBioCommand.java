package dev.revere.commission.discord.command.impl.freelancer;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.services.FreelancerService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/26/2024
 */
@Service
public class SetBioCommand extends SlashCommand {
    private final FreelancerRepository m_freelancerRepository;
    private final FreelancerService m_freelancerService;

    @Autowired
    public SetBioCommand(final FreelancerRepository p_freelancerRepository, FreelancerService p_freelancerService) {
        m_freelancerRepository = p_freelancerRepository;
        m_freelancerService = p_freelancerService;

        this.name = "setbio";
        this.help = "Set your profile bio";
        this.guildOnly = true;

        this.options = List.of(
                new OptionData(OptionType.STRING, "bio", "the bio to display on your profile").setRequired(true)
        );
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final Member member = p_slashCommandEvent.getMember();

        assert member != null;
        if (!m_freelancerRepository.existsFreelancerByUserId(member.getIdLong())) {
            p_slashCommandEvent.reply("You are not currently a freelancer! Feel free to apply.").setEphemeral(true).queue();
            return;
        }

        Freelancer freelancer = m_freelancerRepository.findFreelancerByUserId(member.getIdLong());

        m_freelancerService.setBio(freelancer, p_slashCommandEvent.getOption("bio").getAsString());

        p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Successfully edited your bio")).setEphemeral(true).queue();
    }
}
