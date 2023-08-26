package com.flux.discordbot.discord.event;

import com.flux.discordbot.discord.utility.FluxEmbedBuilder;
import com.flux.discordbot.entities.Commission;
import com.flux.discordbot.repository.CommissionRepository;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class ButtonClickEvent extends ListenerAdapter {
    private final CommissionRepository m_commissionRepository;

    private final Map<Long, ScheduledFuture<?>> deletionTasks = new HashMap<>();
    private final Map<Long, List<Long>> acceptedCommissions = new HashMap<>();

    @Override
    public void onButtonInteraction(@NotNull final ButtonInteractionEvent p_buttonInteractionEvent) {
        final String button = p_buttonInteractionEvent.getComponentId();
        final Member member = p_buttonInteractionEvent.getMember();

        switch (button) {
            case "create-commission" -> {
                StringSelectMenu selectionMenu = StringSelectMenu.create("commission-menu").setPlaceholder("Chose the valid category for your commission")
                        .addOption("Plugin Developer", "1141050234353496088")
                        .addOption("Client Developer", "1141050256675577916")
                        .addOption("Game Developer", "1141050275948404919").build();

                p_buttonInteractionEvent.replyEmbeds(selectCategoryEmbed().getEmbeds()).addActionRow(selectionMenu).setEphemeral(true).queue();
            }
            case "delete-commission" -> {
                Message message = p_buttonInteractionEvent.getChannel().sendMessage(deleteCommissionEmbed()).complete();

                ScheduledFuture<?> deletionTask = Objects.requireNonNull(Objects.requireNonNull(p_buttonInteractionEvent.getGuild()).getTextChannelById(message.getChannel().getId())).delete().queueAfter(10, TimeUnit.SECONDS);
                deletionTasks.put(message.getIdLong(), deletionTask);

            }
            case "cancel-commission-deletion" -> {
                p_buttonInteractionEvent.getChannel().sendMessage(cancelledCommissionDeletion()).queue();
                p_buttonInteractionEvent.getChannel().deleteMessageById(p_buttonInteractionEvent.getMessageId()).queue();

                ScheduledFuture<?> deletionTask = deletionTasks.remove(p_buttonInteractionEvent.getMessageIdLong());
                if (deletionTask != null) {
                    deletionTask.cancel(false);
                }
            }

            // ALL FREELANCERS
            case "accept-commission" -> {
                assert member != null;

                long commissionMessageId = p_buttonInteractionEvent.getMessageIdLong();
                long freelancerId = member.getIdLong();

                acceptedCommissions.computeIfAbsent(freelancerId, k -> new ArrayList<>()).add(commissionMessageId);

                final long channelId = Long.parseLong(p_buttonInteractionEvent.getMessage().getChannel().getId());
                final Commission commission = m_commissionRepository.findCommissionByChannelId(channelId);

                commission.setApprovedFreelancerId(freelancerId);

                m_commissionRepository.save(commission);

                p_buttonInteractionEvent.reply(acceptedCommissionEmbed(member)).queue();
            }

            // ADMINISTRATOR ACTIONS
            case "approve-commission" -> {
                List<SelectOption> options = new ArrayList<>();

                for (Map.Entry<Long, List<Long>> entry : acceptedCommissions.entrySet()) {
                    long freelancerId = entry.getKey();
                    Member freelancer = p_buttonInteractionEvent.getGuild().getMemberById(freelancerId);

                    if (freelancer != null) {
                        String optionLabel = freelancer.getUser().getName() + "'s Commissions";
                        options.add(SelectOption.of(optionLabel, Long.toString(freelancerId)));
                    }
                }

                StringSelectMenu selectionMenu = StringSelectMenu.create("approve-freelancer").setPlaceholder("Choose a freelancer to approve").addOptions(options).build();

                p_buttonInteractionEvent.reply(choseToApprove()).addActionRow(selectionMenu).setEphemeral(true).queue();
            }

            case "disapprove-commission" -> {
                p_buttonInteractionEvent.reply(disapproveEmbed()).queue();
            }
        }
    }

    public MessageCreateData selectCategoryEmbed() {
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("Select the category for your commission")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .addButton(ButtonStyle.PRIMARY, "create-commission", "Create A Commission", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData deleteCommissionEmbed() {
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("Deleting commission in 10 seconds...")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .addButton(ButtonStyle.DANGER, "cancel-commission-deletion", "Cancel Deletion", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData disapproveEmbed() {
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("An administrator has disapproved this commission.")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }

    public MessageCreateData choseToApprove() {
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("Select the freelancer to approve")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }

    public MessageCreateData acceptedCommissionEmbed(final Member p_member) {
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription(p_member.getUser().getName() + " has accepted this commission. Waiting for an administrator to approve.")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .addButton(ButtonStyle.SUCCESS, "approve-commission", "Approve Commission", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.DANGER, "disapprove-commission", "Disapprove Commission", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData cancelledCommissionDeletion() {
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("Cancelled the deletion of commission")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }
}