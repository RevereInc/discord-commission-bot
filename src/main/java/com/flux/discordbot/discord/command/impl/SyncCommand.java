package com.flux.discordbot.discord.command.impl;

import com.flux.discordbot.repository.FreelancerRepository;
import com.flux.discordbot.services.RankSyncService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final Member member = p_slashCommandEvent.getMember();

        assert member != null;
        if (!m_freelancerRepository.existsFreelancerByUserId(member.getIdLong())) {
            p_slashCommandEvent.reply("You are not currently a freelancer! Feel free to apply.").setEphemeral(true).queue();
            return;
        }

        m_rankSyncService.syncMember(member);

        p_slashCommandEvent.reply("Synced your roles in both servers").setEphemeral(true).queue();
    }
}
