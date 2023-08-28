package com.flux.discordbot.discord.command.impl;

import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.entities.TitleDescription;
import com.flux.discordbot.repository.FreelancerRepository;
import com.flux.discordbot.services.FreelancerService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CardCommand extends SlashCommand {
    @Autowired
    public CardCommand(final FreelancerRepository p_freelancerRepository, final FreelancerService p_freelancerService) {

        this.name = "card";
        this.help = "Manage your card displays";
        this.guildOnly = true;

        this.children = new SlashCommand[]{
                new Add(p_freelancerRepository, p_freelancerService),
                new Remove(p_freelancerRepository, p_freelancerService)
        };
    }

    private static class Add extends SlashCommand {
        private final FreelancerRepository m_freelancerRepository;
        private final FreelancerService m_freelancerService;

        public Add(final FreelancerRepository p_freelancerRepository, final FreelancerService p_freelancerService) {
            m_freelancerRepository = p_freelancerRepository;
            m_freelancerService = p_freelancerService;
            this.name = "add";
            this.help = "Add a card to your displayed cards";

            this.options = List.of(
                    new OptionData(OptionType.STRING, "title", "title of the card").setRequired(true),
                    new OptionData(OptionType.STRING, "description", "description of the card").setRequired(true)
            );
        }

        @Override
        protected void execute(final SlashCommandEvent p_slashCommandEvent) {
            final String title = p_slashCommandEvent.getOption("title").getAsString();
            final String description = p_slashCommandEvent.getOption("description").getAsString();

            final User user = p_slashCommandEvent.getUser();

            if (!m_freelancerRepository.existsFreelancerByUserId(user.getIdLong())) {
                p_slashCommandEvent.reply("You are not currently a freelancer! Feel free to apply.").setEphemeral(true).queue();
                return;
            }

            final Freelancer freelancer = m_freelancerRepository.findFreelancerByUserId(user.getIdLong());

            if (m_freelancerService.maxCards(freelancer)) {
                p_slashCommandEvent.reply("You are currently have 3 cards. Try deleting one instead.").setEphemeral(true).queue();
                return;
            }

//            p_slashCommandEvent.deferReply().queue();

            final TitleDescription titleDescription = new TitleDescription(title, description);

            m_freelancerService.addCard(freelancer, titleDescription);

            p_slashCommandEvent.reply("Successfully created card `" + title + "`").setEphemeral(true).queue();
        }
    }

    private static class Remove extends SlashCommand {
        private final FreelancerRepository m_freelancerRepository;
        private final FreelancerService m_freelancerService;

        private Remove(final FreelancerRepository p_freelancerRepository, final FreelancerService p_freelancerService) {
            m_freelancerRepository = p_freelancerRepository;
            m_freelancerService = p_freelancerService;

            this.name = "remove";
            this.help = "Remove a card from your displayed cards";

            this.options = Collections.singletonList(
                    new OptionData(OptionType.NUMBER, "index", "Index of the card (1-3)").setRequired(true)
            );
        }

        @Override
        protected void execute(final SlashCommandEvent p_slashCommandEvent) {
            final int index = p_slashCommandEvent.getOption("index").getAsInt();

            final User user = p_slashCommandEvent.getUser();

            if (!m_freelancerRepository.existsFreelancerByUserId(user.getIdLong())) {
                p_slashCommandEvent.reply("You are not currently a freelancer! Feel free to apply.").setEphemeral(true).queue();
                return;
            }

            final Freelancer freelancer = m_freelancerRepository.findFreelancerByUserId(user.getIdLong());

            final TitleDescription titleDescription = m_freelancerService.removeCard(freelancer, index);

            if (titleDescription == null) {
                p_slashCommandEvent.reply("Unable to find card at index `" + index + "`").setEphemeral(true).queue();
            } else {
                p_slashCommandEvent.reply("Successfully deleted card " + titleDescription.getTitle()).setEphemeral(true).queue();
            }
        }
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        p_slashCommandEvent.reply("Invalid use!").setEphemeral(true).queue();
    }
}
