package dev.revere.commission.discord.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;

/**
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
public class TonicEmbedBuilder {
    private final MessageCreateBuilder m_messageCreateBuilder;
    private final EmbedBuilder m_embedBuilder;
    private final ArrayList<Button> m_buttons = new ArrayList<>();

    public TonicEmbedBuilder() {
        m_messageCreateBuilder = new MessageCreateBuilder();
        m_embedBuilder = new EmbedBuilder();
    }

    public static MessageCreateData sharedMessageEmbed(String p_description) {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(p_description)
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }

    public TonicEmbedBuilder setTitle(@Nullable final String p_title) {
        m_embedBuilder.setTitle(p_title);
        return this;
    }

    public TonicEmbedBuilder setDescription(@Nullable final CharSequence p_description) {
        m_embedBuilder.setDescription(p_description);
        return this;
    }

    public TonicEmbedBuilder setFooter(@Nullable final String p_footer) {
        m_embedBuilder.setFooter(p_footer);
        return this;
    }

    public TonicEmbedBuilder setFooter(@Nullable final String p_footer, @Nullable final String p_imageUrl) {
        m_embedBuilder.setFooter(p_footer);
        return this;
    }

    public TonicEmbedBuilder setImage(@Nullable final String p_image) {
        m_embedBuilder.setImage(p_image);
        return this;
    }

    public TonicEmbedBuilder setThumbnail(@Nullable final String p_image) {
        m_embedBuilder.setThumbnail(p_image);
        return this;
    }

    public TonicEmbedBuilder setColor(final Color p_color) {
        m_embedBuilder.setColor(p_color);
        return this;
    }

    public TonicEmbedBuilder setColor(final int p_color) {
        m_embedBuilder.setColor(p_color);
        return this;
    }

    public TonicEmbedBuilder setTimeStamp(final TemporalAccessor p_temporalAccessor) {
        m_embedBuilder.setTimestamp(p_temporalAccessor);
        return this;
    }

    public TonicEmbedBuilder addField(final String p_name, final String p_value, final boolean p_inline) {
        m_embedBuilder.addField(p_name, p_value, p_inline);
        return this;
    }

    public TonicEmbedBuilder addButton(final ButtonStyle p_buttonStyle, final String p_id, final String p_label, @Nullable final Emoji p_emoji) {
        switch (p_buttonStyle) {
            case PRIMARY -> m_buttons.add(Button.primary(p_id, p_label).withEmoji(p_emoji));
            case SECONDARY -> m_buttons.add(Button.secondary(p_id, p_label).withEmoji(p_emoji));
            case SUCCESS -> m_buttons.add(Button.success(p_id, p_label).withEmoji(p_emoji));
            case DANGER -> m_buttons.add(Button.danger(p_id, p_label).withEmoji(p_emoji));
        }
        return this;
    }

    public TonicEmbedBuilder addDisabledButton(final ButtonStyle p_buttonStyle, final String p_id, final String p_label, @Nullable final Emoji p_emoji) {
        switch (p_buttonStyle) {
            case PRIMARY -> m_buttons.add(Button.primary(p_id, p_label).withEmoji(p_emoji).asDisabled());
            case SECONDARY -> m_buttons.add(Button.secondary(p_id, p_label).withEmoji(p_emoji).asDisabled());
            case SUCCESS -> m_buttons.add(Button.success(p_id, p_label).withEmoji(p_emoji).asDisabled());
            case DANGER -> m_buttons.add(Button.danger(p_id, p_label).withEmoji(p_emoji).asDisabled());
        }
        return this;
    }

    public TonicEmbedBuilder addLinkButton(final String p_url, final String p_label, @Nullable final Emoji p_emoji) {
        m_buttons.add(Button.link(p_url, p_label).withEmoji(p_emoji));
        return this;
    }

    public TonicEmbedBuilder addFile(final FileUpload p_fileUpload) {
        m_messageCreateBuilder.addFiles(p_fileUpload);
        return this;
    }

    public MessageCreateData build() {
        m_messageCreateBuilder.setEmbeds(m_embedBuilder.build());
        if (!m_buttons.isEmpty()) m_messageCreateBuilder.setActionRow(m_buttons);
        return m_messageCreateBuilder.build();
    }
}
