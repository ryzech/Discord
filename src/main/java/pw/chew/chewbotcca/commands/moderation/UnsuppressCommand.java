/*
 * Copyright (C) 2020 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pw.chew.chewbotcca.commands.moderation;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.jdachewtils.command.OptionHelper;

import java.util.Collections;

import static pw.chew.chewbotcca.commands.util.QuoteCommand.retrieveMessageFromLink;

public class UnsuppressCommand extends SlashCommand {

    public UnsuppressCommand() {
        this.name = "unsuppress";
        this.help = "Unsuppresses an embed given a message URL";
        this.botPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
        this.userPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
        this.guildOnly = true;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "message_link", "The message to unsuppress").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String args = OptionHelper.optString(event, "message_link", "");
        Message message;
        try {
            message = retrieveMessageFromLink(args.split("/"), event.getJDA(), null);
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
            return;
        }

        message.suppressEmbeds(false).queue(
            unused -> event.reply("Successfully unsuppressed the embed!").queue(),
            throwable -> event.reply("Could not unsuppress the embed!").queue()
        );
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        Message message;
        try {
            message = getMessage(event, args);
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage());
            return;
        }

        if (message == null) {
            event.reply("Message could not be found or retrieved!");
            return;
        }

        message.suppressEmbeds(false).queue(
            unused -> event.reply("Successfully unsuppressed the embed!"),
            throwable -> event.reply("Could not unsuppress the embed!")
        );
    }

    public Message getMessage(CommandEvent event, String args) {
        Guild server = event.getGuild();
        TextChannel channel = event.getTextChannel();
        Message message;

        // If Copy Link URL
        // 4 => Server
        // 5 => Channel
        // 6 => Message
        if (args.contains("discord.com/channels/")) {
            String[] components = args.split("/");
            if (!components[4].equals(server.getId())) {
                throw new IllegalArgumentException("Server must be the same as this server!");
            }
            if (!components[5].equals(channel.getId())) {
                channel = server.getTextChannelById(components[5]);
            }
            if (channel == null) {
                throw new IllegalArgumentException("This channel does not exist on this server!");
            }
            try {
                return channel.retrieveMessageById(components[6]).complete();
            } catch (MissingAccessException e) {
                throw new IllegalArgumentException("Cannot access that channel!");
            }
        }

        String[] info = args.split(" ");

        if (info.length == 1) {
            // Retrieve the message
            channel.retrieveMessageById(info[0]).complete();
        } else {
            // Get the second (channel id) arg
            String chanId = info[0];
            chanId = chanId.replace("<#", "").replace(">", "");
            // Get the text channel
            channel = event.getJDA().getTextChannelById(chanId);
            // If it's not null
            if (channel != null) {
                return channel.retrieveMessageById(info[1]).complete();
            } else {
                throw new IllegalArgumentException("Invalid Channel ID.");
            }
        }
        return null;
    }
}
