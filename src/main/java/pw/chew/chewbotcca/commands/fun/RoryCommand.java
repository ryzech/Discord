/*
 * Copyright (C) 2021 Chewbotcca
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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.WebhookType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;
import pw.chew.jdachewtils.command.OptionHelper;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class RoryCommand extends SlashCommand {

    public RoryCommand() {
        this.name = "rory";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.children = new SlashCommand[]{new GetRorySubCommand(), new FollowRorySubCommand()};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Unsupported for slash commands with children with options
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        JSONObject rory = new JSONObject(RestClient.get("https://rory.cat/purr/" + event.getArgs()));
        if (rory.has("error")) {
            event.reply(rory.getString("error"));
            return;
        }

        EmbedBuilder embed = generateRoryEmbed(rory);
        if (event.getChannelType() == ChannelType.TEXT && event.getMember().hasPermission(Permission.MANAGE_WEBHOOKS) && event.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            embed.setDescription("Stay up to date with new Rory images by running `" + event.getPrefix() + "rory follow`!");
        }

        event.reply(embed.build());
    }

    private EmbedBuilder generateRoryEmbed(JSONObject rory) {
        String permalink = "https://rory.cat/id/" + rory.getInt("id");

        return new EmbedBuilder()
            .setTitle("Rory :3", permalink)
            .setImage(rory.getString("url") + "?nocache" + Instant.now().getEpochSecond())
            .setFooter("ID: " + rory.getInt("id"));
    }

    public class GetRorySubCommand extends SlashCommand {
        public GetRorySubCommand() {
            this.name = "get";
            this.help = "Gets a Rory photo!";
            this.guildOnly = false;
            this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "id", "The ID of a rory! Leave blank for a random Rory")
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            JSONObject rory = new JSONObject(RestClient.get("https://rory.cat/purr/" + OptionHelper.optString(event, "id", "")));
            if (rory.has("error")) {
                event.reply(rory.getString("error")).setEphemeral(true).queue();
                return;
            }

            EmbedBuilder embed = generateRoryEmbed(rory);
            if (event.getChannelType() == ChannelType.TEXT && event.getMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
                embed.setDescription("Stay up to date with new Rory images by running `/rory follow`!");
            }

            event.replyEmbeds(embed.build()).queue();
        }
    }

    public static class FollowRorySubCommand extends SlashCommand {
        public FollowRorySubCommand() {
            this.name = "follow";
            this.help = "Follows the Rory Image feed to the current channel (Requires Manage Webhooks)";
            this.botPermissions = new Permission[]{Permission.MANAGE_WEBHOOKS};
            this.userPermissions = new Permission[]{Permission.MANAGE_WEBHOOKS};
            this.guildOnly = true;
            this.cooldown = 30;
            this.cooldownScope = CooldownScope.CHANNEL;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            TextChannel destination = event.getTextChannel();
            if (destination.isNews()) {
                event.reply("News channels cannot be followed to other news channels!").setEphemeral(true).queue();
                return;
            }
            if (alreadyFollowing(destination)) {
                event.reply("This channel already has a Rory Images feed! (If not, an error occurred, oopsie)").setEphemeral(true).queue();
                return;
            }
            TextChannel rory = event.getJDA().getTextChannelById("752063016425619487");
            if (rory == null) {
                event.reply("Rory channel not found! :(").setEphemeral(true).queue();
                return;
            }
            rory.follow(destination).queue(yay -> event.reply("Followed the rory images channel successfully, enjoy the Rory :3").queue());
        }

        @Override
        protected void execute(CommandEvent event) {
            TextChannel destination = event.getTextChannel();
            // Not sure if I want to implement selecting another channel yet.
            /*
            if (!event.getArgs().isEmpty()) {
                Object parse = Mention.parseMention(event.getArgs(), event.getGuild(), event.getJDA());
                if (!(parse instanceof GuildChannel)) {
                    destination = event.getGuild().getTextChannelById(event.getArgs());
                } else {
                    destination = (TextChannel) parse;
                }
            }
            if (destination == null) {
                event.reply("Please provide a valid destination!");
                return;
            }
            */
            if (destination.isNews()) {
                event.reply("News channels cannot be followed to other news channels!");
                return;
            }
            if (alreadyFollowing(destination)) {
                event.reply("This channel already has a Rory Images feed! (If not, an error occurred, oopsie)");
                return;
            }
            TextChannel rory = event.getJDA().getTextChannelById("752063016425619487");
            if (rory == null) {
                event.reply("Rory channel not found! :(");
                return;
            }
            rory.follow(destination).queue(yay -> event.reply("Followed the rory images channel successfully, enjoy the Rory :3"));
        }

        public boolean alreadyFollowing(TextChannel channel) {
            List<Webhook> webhooks = channel.retrieveWebhooks().complete();
            for (Webhook webhook : webhooks) {
                if (webhook.getType() != WebhookType.FOLLOWER) {
                    continue;
                }
                Webhook.ChannelReference refChannel = webhook.getSourceChannel();
                if (refChannel != null && refChannel.getId().equals("752063016425619487")) {
                    return true;
                }
            }
            return false;
        }
    }
}
