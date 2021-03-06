package ultimategdbot.commands.impl;

import java.lang.Thread.State;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import ultimategdbot.app.Main;
import ultimategdbot.commands.Command;
import ultimategdbot.commands.CoreCommand;
import ultimategdbot.commands.DiscordCommandHandler;
import ultimategdbot.exceptions.CommandFailedException;
import ultimategdbot.util.AppTools;
import ultimategdbot.util.BotRoles;

/**
 * This command allows the user to interrupt the process of a long-running command,
 * by invoking the kill method of the {@link KillableThread} associated with the command.
 * 
 * @author Alex1304
 *
 */
public class KillCommand extends CoreCommand {

	public KillCommand(EnumSet<BotRoles> rolesRequired) {
		super("kill", rolesRequired);
	}

	@Override
	public void runCommand(MessageReceivedEvent event, List<String> args) throws CommandFailedException {
		int count = 0;
		
		for (String name : Main.THREADS.nameSet()) {
			if (!name.equals(DiscordCommandHandler.generateThreadName(event)) &&
					name.startsWith("command_") &&
					name.matches(".*c" + event.getChannel().getLongID() + ".*")) {
				if (Main.THREADS.getThread(name).getState() != State.TERMINATED)
					count++;
				
				Main.THREADS.killThread(name);
				System.out.println("Killed: " + name);
			}
		}
		Main.THREADS.removeAllTerminated();
		AppTools.sendMessage(event.getChannel(), "Killed " + count + " command(s).");
	}

	@Override
	public String getHelp() {
		return "Kills long-running commands in the current channel.";
	}

	@Override
	public String[] getSyntax() {
		return null;
	}

	@Override
	public String[] getExamples() {
		return null;
	}

	@Override
	protected Map<String, Command> initSubCommandMap() {
		return null;
	}

}
