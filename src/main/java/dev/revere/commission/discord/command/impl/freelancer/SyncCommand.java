package dev.revere.commission.discord.command.impl.freelancer;

import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.services.RankSyncService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class SyncCommand extends SlashCommand {
    private final RankSyncService m_rankSyncService;
    private final FreelancerRepository m_freelancerRepository;

    @Autowired
    public SyncCommand(final RankSyncService p_rankSyncService, final FreelancerRepository p_freelancerRepository) {
        m_rankSyncService = p_rankSyncService;
        m_freelancerRepository = p_freelancerRepository;

        this.name = "sync";
        this.help = "Sync roles for freelancers";
        this.guildOnly = true;

        ArrayList<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "user", "The freelancer to sync roles for", false));
        this.options = options;
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final User user = p_slashCommandEvent.getOption("user") == null
                ? p_slashCommandEvent.getUser()
                : p_slashCommandEvent.getOption("user").getAsUser();

        if (user != p_slashCommandEvent.getUser() && !Objects.requireNonNull(p_slashCommandEvent.getMember()).hasPermission(Permission.ADMINISTRATOR)) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You are missing the `ADMINISTRATOR` permission required to execute this command.")).setEphemeral(true).queue();
            return;
        }

        if (!m_freelancerRepository.existsFreelancerByUserId(user.getIdLong())) {
            if (user == p_slashCommandEvent.getUser()) {
                p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You are not currently a freelancer! Feel free to apply.")).setEphemeral(true).queue();
            } else {
                p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Could not find freelancer with name " + user.getName())).queue();
            }
            return;
        }

        if(m_freelancerRepository.findFreelancerByUserId(user.getIdLong()).getDepartments() == null) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You are not currently a freelancer! Feel free to apply.")).queue();
            return;
        }

        Member member = Objects.requireNonNull(p_slashCommandEvent.getGuild()).retrieveMemberById(user.getId()).complete();

        m_rankSyncService.syncMember(member, m_freelancerRepository.findFreelancerByUserId(user.getIdLong()));

        p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Successfully synced your roles in all servers")).setEphemeral(true).queue();
    }
}
