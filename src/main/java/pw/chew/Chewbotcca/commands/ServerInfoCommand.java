package pw.chew.Chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.awaitility.core.ConditionTimeoutException;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
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
        String id = event.getArgs();

        Guild server;
        if(id.length() > 0) {
            server = event.getJDA().getGuildById(id);
            if (server == null) {
                event.reply("I am not on that server and are therefore unable to view that server's stats. Try getting them to add me by sending them this invite link: <http://bit.ly/Chewbotcca>");
                return;
            }
        } else {
            server = event.getGuild();
        }

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

        switch(server.getRegion()) {
            case VIP_AMSTERDAM:
                e.addField("Server Region", "<:region_amsterdam:718523705080152136> <:vip_region:718523836823240814> Amsterdam", true);
                break;
            case BRAZIL:
                e.addField("Server Region", "<:region_brazil:718523705055248418> Brazil", true);
                break;
            case EU_CENTRAL:
                e.addField("Server Region", "<:region_eu:718523704979488820> Central Europe", true);
                break;
            case HONG_KONG:
                e.addField("Server Region", "<:region_hongkong:718523705105580103> Hong Kong", true);
                break;
            case JAPAN:
                e.addField("Server Region", "<:region_japan:718523704853790892> Japan", true);
                break;
            case RUSSIA:
                e.addField("Server Region", "<:region_russia:718523705193660486> Russia", true);
                break;
            case SINGAPORE:
                e.addField("Server Region", "<:region_singapore:718523705583730768> Singapore", true);
                break;
            case SYDNEY:
                e.addField("Server Region", "<:region_sydney:718523704879087709> Sydney", true);
                break;
            case US_CENTRAL:
                e.addField("Server Region", "<:region_us:718523704845533227> US Central", true);
                break;
            case US_EAST:
                e.addField("Server Region", "<:region_us:718523704845533227> US East", true);
                break;
            case VIP_US_EAST:
                e.addField("Server Region", "<:region_us:718523704845533227> <:vip_region:718523836823240814> US East", true);
                break;
            case US_SOUTH:
                e.addField("Server Region", "<:region_us:718523704845533227> US South", true);
                break;
            case US_WEST:
                e.addField("Server Region", "<:region_us:718523704845533227> US West", true);
                break;
            case VIP_US_WEST:
                e.addField("Server Region", "<:region_us:718523704845533227> <:vip_region:718523836823240814> US West", true);
                break;
            case EU_WEST:
                e.addField("Server Region", "<:region_eu:718523704979488820> Western Europe", true);
                break;
            case INDIA:
            case EUROPE:
            case LONDON:
            case UNKNOWN:
            case AMSTERDAM:
            case FRANKFURT:
            case VIP_JAPAN:
            case VIP_BRAZIL:
            case VIP_LONDON:
            case VIP_SYDNEY:
            case SOUTH_KOREA:
            case VIP_EU_WEST:
            case SOUTH_AFRICA:
            case VIP_US_SOUTH:
            case VIP_FRANKFURT:
            case VIP_SINGAPORE:
            case VIP_EU_CENTRAL:
            case VIP_US_CENTRAL:
            case VIP_SOUTH_KOREA:
            case VIP_SOUTH_AFRICA:
                e.addField("Server Region", server.getRegionRaw(), true);
                break;
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

        e.addField("Channel Count", "Total: " + totalchans + "\n" +
                "Text: " + textchans + " (" + textpercent + "%)\n" +
                "Voice: " + voicechans + " (" + voicepercent + "%)\n" +
                "Categories: " + categories + " (" + catepercent + "%)\n" +
                "Store Pages: " + storechans + " (" + storepercent + "%)", true);

        e.addField("Server Boosting", "Level: " + server.getBoostTier().getKey() + "\nBoosters: " + server.getBoostCount(), true);

        List<CharSequence> perks = new ArrayList<>();
        if(server.getVanityCode() != null)
            perks.add("Vanity Code: " + "[" + server.getVanityCode() + "](https://discord.gg/" + server.getVanityCode() + ")");
        for(int i = 0; i < server.getFeatures().size(); i++) {
            perks.add((CharSequence) server.getFeatures().toArray()[i]);
        }

        if(perks.size() > 0)
            e.addField("Perks", String.join("\n", perks), true);

        StringBuilder roleNames = new StringBuilder();

        List<Role> roles = server.getRoles();
        for (int i=0; i < roles.size() && i < 50; i++) {
            Role role = roles.get(i);
            roleNames.append(role.getAsMention()).append(" ");
        }

        String roleName = roleNames.toString();
        if(roleName.length() > 1024)
            roleName = roleName.substring(0, 1023);
        e.addField("Roles - " + roles.size(), roleName, false);

        e.setFooter("Server Created on");
        e.setTimestamp(server.getTimeCreated());

        e.setColor(event.getSelfMember().getColor());

        event.reply(e.build());

    }
}

