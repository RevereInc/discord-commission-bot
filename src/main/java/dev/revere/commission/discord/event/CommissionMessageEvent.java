package dev.revere.commission.discord.event;

import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.CommissionRepository;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.Map;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/26/2024
 */
@Service
@AllArgsConstructor
public class CommissionMessageEvent extends ListenerAdapter {
    private final CommissionRepository m_commissionRepository;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        Message message = event.getMessage();
        Message referencedMessage = message.getReferencedMessage();

        if (referencedMessage == null) return;

        Commission commission = m_commissionRepository.findCommissionByChannelId(event.getChannel().getIdLong());
        if (commission == null) {
            commission = m_commissionRepository.findCommissionByPublicChannelId(event.getChannel().getIdLong());
        }
        if (commission == null) return;

        handleMessageReply(event, commission, message, referencedMessage);
    }

    private void handleMessageReply(MessageReceivedEvent event, Commission commission, Message message, Message referencedMessage) {
        boolean isClientChannel = event.getChannel().getIdLong() == commission.getPublicChannelId();
        boolean isFreelancerChannel = event.getChannel().getIdLong() == commission.getChannelId();

        if (isClientChannel) {
            handleClientMessage(commission, message, referencedMessage);
        } else if (isFreelancerChannel) {
            handleFreelancerMessage(commission, message, referencedMessage);
        }
    }

    private void handleClientMessage(Commission commission, Message message, Message referencedMessage) {
        if (referencedMessage.getIdLong() == commission.getInitialClientMessageId() ||
                commission.getFreelancerMessages().containsValue(referencedMessage.getIdLong())) {

            TextChannel freelancerChannel = message.getJDA().getTextChannelById(commission.getChannelId());
            if (freelancerChannel != null) {
                MessageCreateData embedData = createMessageEmbed(message.getAuthor().getName(), message.getContentRaw());

                Long originalMessageId = null;
                for (Map.Entry<Long, Long> entry : commission.getFreelancerMessages().entrySet()) {
                    if (entry.getValue().equals(referencedMessage.getIdLong())) {
                        originalMessageId = entry.getKey();
                        break;
                    }
                }

                if (originalMessageId != null) {
                    freelancerChannel.retrieveMessageById(originalMessageId).queue(originalMessage -> {
                        freelancerChannel.sendMessage(embedData).setMessageReference(originalMessage).queue(sentMessage -> {
                            commission.getClientMessages().put(message.getIdLong(), sentMessage.getIdLong());
                            m_commissionRepository.save(commission);
                        });
                    });
                } else {
                    freelancerChannel.sendMessage(embedData).queue(sentMessage -> {
                        commission.getClientMessages().put(message.getIdLong(), sentMessage.getIdLong());
                        m_commissionRepository.save(commission);
                    });
                }
            }
        }
    }

    private void handleFreelancerMessage(Commission commission, Message message, Message referencedMessage) {
        if (referencedMessage.getIdLong() == commission.getInitialFreelancerMessageId() ||
                commission.getClientMessages().containsValue(referencedMessage.getIdLong())) {

            TextChannel clientChannel = message.getJDA().getTextChannelById(commission.getPublicChannelId());
            if (clientChannel != null) {
                MessageCreateData embedData = createMessageEmbed(message.getAuthor().getName(), message.getContentRaw());

                Long originalMessageId = null;
                for (Map.Entry<Long, Long> entry : commission.getClientMessages().entrySet()) {
                    if (entry.getValue().equals(referencedMessage.getIdLong())) {
                        originalMessageId = entry.getKey();
                        break;
                    }
                }

                if (originalMessageId != null) {
                    clientChannel.retrieveMessageById(originalMessageId).queue(originalMessage -> {
                        clientChannel.sendMessage(embedData).setMessageReference(originalMessage).queue(sentMessage -> {
                            commission.getFreelancerMessages().put(message.getIdLong(), sentMessage.getIdLong());
                            m_commissionRepository.save(commission);
                        });
                    });
                } else {
                    clientChannel.sendMessage(embedData).queue(sentMessage -> {
                        commission.getFreelancerMessages().put(message.getIdLong(), sentMessage.getIdLong());
                        m_commissionRepository.save(commission);
                    });
                }
            }
        }
    }

    private MessageCreateData createMessageEmbed(String p_author, String p_content) {
        String description = String.format(
                """
                        You have received a new message from **%s**:
                        ### <:RVC_Chat:1299484561637638185> Message:
                        - %s
                                                
                        *Reply to this message to communicate with the other party*
                        """,
                p_author,
                p_content
        );

        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(description)
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }

    public void setupInitialMessages(Commission commission, JDA jda) {
        TextChannel clientChannel = jda.getTextChannelById(commission.getPublicChannelId());
        if (clientChannel != null) {
            String clientDescription = """
                    You have started a new commission thread.
                    ### <:RVC_Chat:1299484561637638185> Instructions:
                    - Reply to this message to communicate with the freelancer
                                
                    *Your messages will be relayed to the freelancer automatically*
                    """;
            MessageCreateData clientEmbed = new TonicEmbedBuilder()
                    .setTitle(" ")
                    .setDescription(clientDescription)
                    .setTimeStamp(Instant.now())
                    .setColor(Color.decode("#2b2d31"))
                    .build();

            clientChannel.sendMessage(clientEmbed).queue(message -> {
                commission.setInitialClientMessageId(message.getIdLong());
                m_commissionRepository.save(commission);
            });
        }

        TextChannel freelancerChannel = jda.getTextChannelById(commission.getChannelId());
        if (freelancerChannel != null) {
            String freelancerDescription = """
                    A new commission thread has been created.
                    ### <:RVC_Chat:1299484561637638185> Instructions:
                    - Reply to this message to communicate with the client
                                
                    *Your messages will be relayed to the client automatically*
                    """;
            MessageCreateData freelancerEmbed = new TonicEmbedBuilder()
                    .setTitle(" ")
                    .setDescription(freelancerDescription)
                    .setTimeStamp(Instant.now())
                    .setColor(Color.decode("#2b2d31"))
                    .build();

            freelancerChannel.sendMessage(freelancerEmbed).queue(message -> {
                commission.setInitialFreelancerMessageId(message.getIdLong());
                m_commissionRepository.save(commission);
            });
        }
    }
}
