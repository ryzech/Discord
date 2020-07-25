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
package pw.chew.chewbotcca.commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

// %^ping command
public class PingCommand extends Command {

    public PingCommand() {
        this.name = "ping";
        this.help = "Ping the bot";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get the timestamp of the ping message
        long time = commandEvent.getMessage().getTimeCreated().toInstant().toEpochMilli();
        // Send a "Checking ping" message and calculate the difference between this message and the %^ping message
        commandEvent.getChannel().sendMessage(new EmbedBuilder().setDescription("Checking ping..").build()).queue((msg) -> {
            EmbedBuilder eb = new EmbedBuilder().setDescription("Ping is " + (msg.getTimeCreated().toInstant().toEpochMilli() - time) + "ms");
            msg.editMessage(eb.build()).queue();
        });
        // Not sure why this is here. Probably why i'm commenting now
        commandEvent.getChannel().getLatestMessageId();
    }
}