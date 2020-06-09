package pw.chew.Chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.awaitility.core.ConditionTimeoutException;
import pw.chew.Chewbotcca.util.DateTime;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class ServerInfoCommand extends Command {
    public ServerInfoCommand() {
        this.name = "serverinfo";
        this.aliases = new String[]{"sinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        Guild server = event.getGuild();

        try {
            event.getGuild().retrieveMembers().get();
            await().atMost(30, TimeUnit.SECONDS).until(() -> event.getGuild().getMemberCache().size() == event.getGuild().getMemberCount());
        } catch (InterruptedException | ExecutionException interruptedException) {
            interruptedException.printStackTrace();
        }

        if(args.contains("boost")) {
            event.reply(gatherBoostInfo(server).build());
        } else if(args.contains("role")) {
            event.reply(gatherRoles(server).build());
        } else if(args.contains("bot")) {
            event.reply(gatherBots(server).build());
        } else {
            event.reply(gatherMainInfo(event, server).build());
        }
    }

    public EmbedBuilder gatherMainInfo(CommandEvent event, Guild server) {
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Server Information");
        e.setAuthor(server.getName(), null,server.getIconUrl());

        e.setThumbnail(server.getIconUrl());

        server.retrieveOwner(true).queue();
        try {
            await().atMost(3, TimeUnit.SECONDS).until(() -> server.getOwner() != null);
            assert server.getOwner() != null;
            e.addField("Server Owner", server.getOwner().getAsMention(), true);
        } catch (ConditionTimeoutException error) {
            e.addField("Server Owner", "Timed out retrieving owner...", true);
        }

        e.addField("Server ID", server.getId(), true);

        switch (server.getRegion()) {
            case VIP_AMSTERDAM -> e.addField("Server Region", "<:region_amsterdam:718523705080152136> <:vip_region:718523836823240814> Amsterdam", true);
            case BRAZIL -> e.addField("Server Region", "<:region_brazil:718523705055248418> Brazil", true);
            case EU_CENTRAL -> e.addField("Server Region", "<:region_eu:718523704979488820> Central Europe", true);
            case HONG_KONG -> e.addField("Server Region", "<:region_hongkong:718523705105580103> Hong Kong", true);
            case JAPAN -> e.addField("Server Region", "<:region_japan:718523704853790892> Japan", true);
            case RUSSIA -> e.addField("Server Region", "<:region_russia:718523705193660486> Russia", true);
            case SINGAPORE -> e.addField("Server Region", "<:region_singapore:718523705583730768> Singapore", true);
            case SYDNEY -> e.addField("Server Region", "<:region_sydney:718523704879087709> Sydney", true);
            case US_CENTRAL -> e.addField("Server Region", "<:region_us:718523704845533227> US Central", true);
            case US_EAST -> e.addField("Server Region", "<:region_us:718523704845533227> US East", true);
            case VIP_US_EAST -> e.addField("Server Region", "<:region_us:718523704845533227> <:vip_region:718523836823240814> US East", true);
            case US_SOUTH -> e.addField("Server Region", "<:region_us:718523704845533227> US South", true);
            case US_WEST -> e.addField("Server Region", "<:region_us:718523704845533227> US West", true);
            case VIP_US_WEST -> e.addField("Server Region", "<:region_us:718523704845533227> <:vip_region:718523836823240814> US West", true);
            case EU_WEST -> e.addField("Server Region", "<:region_eu:718523704979488820> Western Europe", true);
            case INDIA, EUROPE, LONDON, UNKNOWN, AMSTERDAM, FRANKFURT, VIP_JAPAN, VIP_BRAZIL, VIP_LONDON, VIP_SYDNEY, SOUTH_KOREA, VIP_EU_WEST, SOUTH_AFRICA, VIP_US_SOUTH, VIP_FRANKFURT, VIP_SINGAPORE, VIP_EU_CENTRAL, VIP_US_CENTRAL -> e.addField("Server Region", "<:region_us:718523704845533227> <:vip_region:718523836823240814> US Central", true);
            case VIP_SOUTH_KOREA, VIP_SOUTH_AFRICA -> e.addField("Server Region", server.getRegionRaw(), true);
        }

        try {
            event.getGuild().retrieveMembers().get();
            await().atMost(30, TimeUnit.SECONDS).until(() -> event.getGuild().getMemberCache().size() == event.getGuild().getMemberCount());
        } catch (InterruptedException | ExecutionException interruptedException) {
            interruptedException.printStackTrace();
        }

        List<Member> members = server.getMembers();
        int bots = 0;
        for (Member member : members) {
            if (member.getUser().isBot())
                bots += 1;
        }

        int membercount = server.getMemberCount();
        int humans = membercount - bots;

        DecimalFormat df = new DecimalFormat("#.##");

        String botpercent = df.format((float)bots / (float)membercount * 100);
        String humanpercent = df.format((float)humans / (float)membercount * 100);

        e.addField("Member Count", "Total: " + membercount + "\n" +
                "Bots: " + bots + " - (" + botpercent + "%)\n" +
                "Users: " + humans + " - (" + humanpercent + "%)", true);

        int totalchans = server.getChannels().size();
        int textchans = server.getTextChannels().size();
        int voicechans = server.getVoiceChannels().size();
        int categories = server.getCategories().size();
        int storechans = server.getStoreChannels().size();

        String textpercent = df.format((float)textchans / (float)totalchans * 100);
        String voicepercent = df.format((float)voicechans / (float)totalchans * 100);
        String catepercent = df.format((float)categories / (float)totalchans * 100);
        String storepercent = df.format((float)storechans / (float)totalchans * 100);

        List<CharSequence> counts = new ArrayList<>();
        counts.add("Total: " + totalchans);
        counts.add("Text: " + textchans + " (" + textpercent + "%)");
        counts.add("Voice: " + voicechans + " (" + voicepercent + "%)");
        counts.add("Categories: " + categories + " (" + catepercent + "%)");
        if(server.getFeatures().contains("COMMERCE"))
            counts.add("Store Pages: " + storechans + " (" + storepercent + "%)");

        e.addField("Channel Count", String.join("\n", counts), true);

        if(server.getBoostCount() > 0)
            e.addField("Server Boosting",
                    "Level: " + server.getBoostTier().getKey() +
                            "\nBoosts: " + server.getBoostCount() +
                            "\nBoosters: " + server.getBoosters().size(), true);

        String perks = perkParser(server);

        if(perks.length() > 0)
            e.addField("Perks", perks, true);

        e.addField("View More Info", "Roles - `%^sinfo roles`\nBoosts - `%^sinfo boosts`\nBots - `%^sinfo bots`", false);

        e.setFooter("Server Created on");
        e.setTimestamp(server.getTimeCreated());

        e.setColor(event.getSelfMember().getColor());

        return e;
    }

    public EmbedBuilder gatherBoostInfo(Guild server) {
        List<Member> boosters = server.getBoosters();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Boosters for " + server.getName());
        List<CharSequence> boostString = new ArrayList<>();
        Instant now = Instant.now();
        for (Member booster : boosters) {
            boostString.add(booster.getAsMention() + " for " + DateTime.timeAgo(now.toEpochMilli() - booster.getTimeBoosted().toInstant().toEpochMilli()));
        }
        embed.setDescription(String.join("\n", boostString));
        if(boostString.size() == 0) {
            embed.setDescription("No one is boosting! Will you be the first?");
        }
        return embed;
    }

    public EmbedBuilder gatherRoles(Guild server) {
        EmbedBuilder e = new EmbedBuilder();

        e.setTitle("Role List for " + server.getName());

        StringBuilder roleNames = new StringBuilder();

        roleNames.append("Members - Role Mention").append("\n");
        roleNames.append("Note: Roles that are integrations are skipped!").append("\n");

        List<Role> roles = server.getRoles();
        for (int i=0; i < roles.size() && i < 50; i++) {
            Role role = roles.get(i);
            List<Member> membersWithRole = server.getMembersWithRoles(role);
            int members = membersWithRole.size();
            boolean skip = false;
            if(role.isManaged() && members == 1 && membersWithRole.get(0).getUser().isBot())
                skip = true;
            if(role.isPublicRole())
                skip = true;

            if(!skip)
                roleNames.append(membersWithRole.size()).append(" - ").append(role.getAsMention()).append("\n");
        }

        String roleName = roleNames.toString();
        e.setDescription(roleName);

        return e;
    }

    public EmbedBuilder gatherBots(Guild server) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Bots on " + server.getName());
        List<CharSequence> bots = new ArrayList<>();
        bots.add("Newest bots on the bottom");
        Member[] members = server.getMembers().toArray(new Member[0]);
        Arrays.sort(members, (o1, o2) -> {
            if (o1.getTimeJoined().toEpochSecond() > o2.getTimeJoined().toEpochSecond())
                return 1;
            else if (o1.getTimeJoined() == o2.getTimeJoined())
                return 0;
            else
                return -1;
        });
        for (Member member : members) {
            if (member.getUser().isBot())
                bots.add(member.getAsMention() + " added " + DateTime.timeAgo(Instant.now().toEpochMilli() - member.getTimeJoined().toInstant().toEpochMilli(), false) + " ago");
        }
        embed.setDescription(String.join("\n", bots));
        return embed;
    }

    public String perkParser(Guild server) {

        List<CharSequence> perks = new ArrayList<>();
        String[] features = server.getFeatures().toArray(new String[0]);
        Arrays.sort(features);
        for(int i = 0; i < server.getFeatures().size(); i++) {
            switch (features[i]) {
                default -> perks.add(capitalize(features[i]));
                case "BANNER" -> perks.add("[Banner](" + server.getBannerUrl() + ")");
                case "INVITE_SPLASH" -> perks.add("[Invite Splash](" + server.getSplashUrl() + ")");
                case "VANITY_URL" -> perks.add("Vanity URL: " + "[" + server.getVanityCode() + "](https://discord.gg/" + server.getVanityCode() + ")");
            }
        }

        return String.join("\n", perks);
    }

    /*
    Source: https://github.com/ChewMC/TransmuteIt/blob/2b86/src/pw/chew/transmuteit/DiscoveriesCommand.java#L174-L186
     */
    public String capitalize(String to) {
        if(to.equals("")) {
            return "";
        }
        String[] words = to.split("_");
        StringBuilder newword = new StringBuilder();
        for (String word : words) {
            String rest = word.substring(1).toLowerCase();
            String first = word.substring(0, 1).toUpperCase();
            newword.append(first).append(rest).append(" ");
        }
        return newword.toString();
    }
}

