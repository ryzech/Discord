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
package pw.chew.chewbotcca.commands.services;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import me.memerator.api.MemeratorAPI;
import me.memerator.api.entity.Age;
import me.memerator.api.entity.UserPerk;
import me.memerator.api.errors.NotFound;
import me.memerator.api.object.Meme;
import me.memerator.api.object.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.jdachewtils.command.OptionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemeratorCommand extends SlashCommand {
    private static final MemeratorAPI api = new MemeratorAPI(PropertiesManager.getMemeratorKey());

    public MemeratorCommand() {
        this.name = "memerator";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.children = new SlashCommand[]{new MemeratorMemeSubCommand(), new MemeratorUserSubCommand()};
    }

    // Base methods for Slash Commands aren't added
    @Override
    protected void execute(SlashCommandEvent event) {}

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply("""
            Please specify meme/user to search for. Examples:
            `%^memerator meme aaaaaaa` => Get meme `aaaaaaa`
            `%^memerator meme bruh moment` => Search for memes containing "bruh moment"
            `%^memerator user Chewbotcca` => Find a user by username
            """);
    }

    public static MemeratorAPI getAPI() {
        return api;
    }

    public static class MemeratorMemeSubCommand extends SlashCommand {

        public MemeratorMemeSubCommand() {
            this.name = "meme";
            this.help = "Gets a meme from Memerator";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;
            this.cooldown = 5;
            this.cooldownScope = CooldownScope.USER;
            this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "meme", "The meme to get by ID, search for, or random for random.").setRequired(true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            try {
                event.replyEmbeds(buildMemeEmbed(OptionHelper.optString(event, "meme", ""), event.getChannel())).queue();
            } catch (IllegalArgumentException e) {
                event.replyEmbeds(ResponseHelper.generateFailureEmbed("The meme machine ran dry...", e.getMessage()))
                    .setEphemeral(true)
                    .queue();
            }
        }

        @Override
        protected void execute(CommandEvent event) {
            event.getChannel().sendTyping().queue();
            try {
                event.reply(buildMemeEmbed(event.getArgs(), event.getChannel()));
            } catch (IllegalArgumentException e) {
                event.reply(ResponseHelper.generateFailureEmbed("The meme machine ran dry...", e.getMessage()));
            }
        }

        private MessageEmbed buildMemeEmbed(String args, MessageChannel channel) {
            boolean id = args.toLowerCase().matches("([a-f]|[0-9]){6,7}");
            Meme meme;
            if (args.equalsIgnoreCase("random")) {
                meme = api.getRandomMeme();
            } else {
                meme = getMeme(args, id);
            }
            if (meme == null) {
                throw new IllegalArgumentException("No memes found for query.");
            }

            EmbedBuilder eb = generateMemeEmbed(meme);
            if (channel.getType() == ChannelType.TEXT) {
                TextChannel textChannel = (TextChannel) channel;
                if (!textChannel.isNSFW() && meme.getAgeRating() == Age.MATURE) {
                    throw new IllegalArgumentException("This meme is marked as Mature and this channel is not a NSFW channel!");
                }
            }

            if (meme.getAuthor().isProActive()) {
                eb.setColor(meme.getAuthor().getNameColor());
            }

            return eb.build();
        }

        public static Meme getMeme(String args, boolean id) {
            if (id) {
                try {
                    return api.getMeme(args);
                } catch (NotFound notFound) {
                    return null;
                }
            } else {
                List<Meme> response = api.searchMemes(args);
                if (response.isEmpty()) {
                    return null;
                } else {
                    return response.get(0);
                }
            }
        }

        public static EmbedBuilder generateMemeEmbed(Meme meme) {
            String captionOrNah;
            if(meme.getCaption() == null || meme.getCaption().equals("")) {
                captionOrNah = "*Author hasn't set a caption yet!*";
            } else {
                captionOrNah = meme.getCaption();
            }
            String authorString = "[" + meme.getAuthor().getUsername() + "](" + meme.getAuthor().getProfileUrl() + ")";
            if (meme.getAuthor().getUserPerks().contains(UserPerk.VERIFIED)) {
                authorString = authorString + " <:verified:595298760502935573>";
            }

            return new EmbedBuilder()
                .setTitle("Meme Information (" + meme.getMemeId() + ")", meme.getMemeUrl())
                .addField("Author", authorString, true)
                .addField("Age Rating", meme.getAgeRating().toString(), true)
                .addField("Submitted", meme.getTimeAgoFormatted() + " ago", true)
                .addField("Caption", captionOrNah, false)
                .addField("Ratings", meme.getAverageRating() + " average from " + meme.getTotalRatings() + " meme reviewers", false)
                .setFooter("Meme submitted")
                .setTimestamp(meme.getTimestamp())
                .setImage(meme.getImageUrl());
        }
    }

    public static class MemeratorUserSubCommand extends SlashCommand {

        public MemeratorUserSubCommand() {
            this.name = "profile";
            this.aliases = new String[]{"user"};
            this.help = "Gets a profile from Memerator";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;
            this.cooldown = 5;
            this.cooldownScope = CooldownScope.USER;
            this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "user", "The user to lookup by name/id").setRequired(true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            try {
                User user = api.getUser(OptionHelper.optString(event, "user", ""));
                event.replyEmbeds(generateUserEmbed(user).build()).queue();
            } catch (NotFound notFound) {
                event.reply("User not found!").setEphemeral(true).queue();
            }
        }

        @Override
        protected void execute(CommandEvent commandEvent) {
            commandEvent.getChannel().sendTyping().queue();
            User user;
            try {
                user = api.getUser(commandEvent.getArgs());
            } catch (NotFound notFound) {
                commandEvent.reply("User not found!");
                return;
            }
            commandEvent.reply(generateUserEmbed(user).build());
        }

        public static EmbedBuilder generateUserEmbed(User user) {
            List<CharSequence> perks = new ArrayList<>();
            List<UserPerk> perkList = user.getUserPerks();
            if(perkList.contains(UserPerk.FOUNDER))
                perks.add("Founder");
            if(perkList.contains(UserPerk.STAFF))
                perks.add("<:staffbadge:712859087804694548> Staff");
            if(perkList.contains(UserPerk.VERIFIED))
                perks.add("<:verified:595298760502935573> Verified");
            if(perkList.contains(UserPerk.PRO))
                perks.add("<:pro:595298760687353873> Pro");
            if(perkList.contains(UserPerk.TRANSLATOR))
                perks.add("Translator");
            if(perkList.contains(UserPerk.SERVICE))
                perks.add("Service");
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(user.getUsername() + "'s Profile", user.getProfileUrl())
                .setDescription(user.getBio())
                .addField("Stats", "Memes: " + user.getMemeCount() + "\nFollowers: " + user.getFollowerCount() + "\nFollowing: " + user.getFollowingCount(), true)
                .setFooter("Joined")
                .setTimestamp(user.getJoinTimestamp());
            if (user.isProActive()) {
                embed.setColor(user.getNameColor());
            }
            if(!perks.isEmpty())
                embed.addField("Status", String.join("\n", perks), true);
            return embed;
        }
    }
}
