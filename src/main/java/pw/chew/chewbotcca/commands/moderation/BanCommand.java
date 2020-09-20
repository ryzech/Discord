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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import pw.chew.chewbotcca.util.Mention;

import java.util.Arrays;

public class BanCommand extends Command {
    public BanCommand() {
        this.name = "ban";
        this.botPermissions = new Permission[]{Permission.BAN_MEMBERS, Permission.MESSAGE_EMBED_LINKS};
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        if(args.length == 0) {
            event.reply("Sorry, but you need to mention the person you want to ban");
            return;
        }

        User user;
        try {
            user = Mention.parseUserMention(args[0], event.getJDA());
        } catch (IllegalArgumentException e) {
            user = event.getJDA().getUserById(args[0]);
        }

        if (user == null) {
            event.reply("Could not find user in question. Make sure they exist!");
            return;
        }

        int days = 0;
        if (args.length > 1) {
            try {
                days = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (!Arrays.asList(0, 1, 7).contains(days)) {
            event.reply("Message deletion in days must be one of: 0, 1, 7.");
            return;
        }

        User finalUser = user;
        int finalDays = days;
        try {
            event.getGuild().ban(user, days).queue(yay -> {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("Somebody order a ban hammer?");
                embed.setColor(0x7d5eba);
                embed.setDescription("That dude? Finna yeeted the guy. We don't need troublemakers here! Begone!\n" +
                    "You just banned some DUDE (aka " + finalUser.getAsTag() + ").\n" +
                    "How much history got deleted? " + finalDays + " days...");

                embed.setAuthor("The banner (you): " + event.getAuthor().getAsTag());

                event.reply(embed.build());
            });
        } catch (HierarchyException e) {
            event.reply("Well, I couldn't ban the guy. Reason: That user has a role higher or equal to my highest role.\n" +
                "Please make sure this is fixed and try again, thanks!");
        }
    }
}
