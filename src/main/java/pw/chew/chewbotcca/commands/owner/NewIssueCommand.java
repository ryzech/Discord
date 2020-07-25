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
package pw.chew.chewbotcca.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.kohsuke.github.*;
import pw.chew.chewbotcca.util.PropertiesManager;

import java.io.IOException;

// %^newissue command
public class NewIssueCommand extends Command {

    public NewIssueCommand() {
        this.name = "issue";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        GitHub github;
        GHRepository repo;
        commandEvent.getChannel().sendTyping().queue();
        try {
            github = new GitHubBuilder().withOAuthToken(PropertiesManager.getGithubToken()).build();
            repo = github.getRepository("Chewbotcca/Discord");
        } catch (IOException e) {
            e.printStackTrace();
            commandEvent.reply("Error occurred initializing GitHub. How did this happen?");
            return;
        }

        String args = commandEvent.getArgs();

        boolean hasDescription = args.contains("--description");
        boolean hasLabel = args.contains("--label");

        String title;
        String description = null;
        String label = null;
        if(!hasDescription && !hasLabel) {
            title = args;
        } else if (hasDescription && !hasLabel) {
            title = args.split(" --description")[0];
            description = args.split("--description ")[1];
        } else {
            title = args.split(" --description")[0];
            description = args.split("--description ")[1].split(" --label")[0];
            label = args.split("--label ")[1];
        }

        try {
            GHIssueBuilder issueBuilder = repo.createIssue(title).assignee(github.getMyself());
            if(hasDescription)
                issueBuilder.body(description);
            if(hasLabel)
                issueBuilder.label(label);
            GHIssue issue = issueBuilder.create();
            commandEvent.reply("Issue created @ " + issue.getUrl().toString().replace("api.github.com/repos", "github.com"));
        } catch (IOException e) {
            e.printStackTrace();
            commandEvent.reply("Error occurred creating Issue, check console for more information.");
        }
    }
}