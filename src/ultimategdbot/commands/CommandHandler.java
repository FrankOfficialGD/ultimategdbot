package ultimategdbot.commands;

import static ultimategdbot.app.Main.CMD_PREFIX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import ultimategdbot.app.AppTools;
import ultimategdbot.commands.impl.ChangeBotUsernameCommand;
import ultimategdbot.commands.impl.GDEventsCommand;
import ultimategdbot.commands.impl.HelpCommand;
import ultimategdbot.commands.impl.PingCommand;
import ultimategdbot.commands.impl.SetupCommand;
import ultimategdbot.exceptions.CommandFailedException;

/**
 * Bot commands are handled here, using the Discord API based on events
 * 
 * @author Alex1304
 *
 */
public class CommandHandler {

	/**
	 * Maps that associates text commands to their actions.
	 */
	public static Map<String, Command> commandMap = new HashMap<>();
	public static Map<String, Command> superadminCommandMap = new HashMap<>();
	public static Map<String, Command> adminCommandMap = new HashMap<>();

	/**
	 * Constructor
	 */
	public CommandHandler() {
		loadCommandMaps();
	}

	/**
	 * Loads the command map so they are recognized by the handler
	 */
	private void loadCommandMaps() {
		// Superadmin commands
		superadminCommandMap.put("changebotusername", new ChangeBotUsernameCommand());
		
		// Admin commands
		adminCommandMap.put("setup", new SetupCommand());
		
		// Public commands
		commandMap.put("ping", new PingCommand());
		commandMap.put("gdevents", new GDEventsCommand());
		commandMap.put("help", new HelpCommand());
	}

	/**
	 * Handles messages sent in the guild and execute commands
	 * 
	 * @param event
	 */
	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event) {
		// Note for error handling, you'll probably want to log failed commands
		// with a logger or sout
		// In most cases it's not advised to annoy the user with a reply incase
		// they didn't intend to trigger a
		// command anyway, such as a user typing ?notacommand, the bot should
		// not say "notacommand" doesn't exist in
		// most situations. It's partially good practise and partially developer
		// preference

		// Given a message "/test arg1 arg2", argArray will contain ["/test",
		// "arg1", "arg2"]
		String[] argArray = event.getMessage().getContent().split(" ");

		// First ensure at least the command and prefix is present, the arg
		// length can be handled by your command func
		if (argArray.length == 0)
			return;

		// Check if the first arg (the command) starts with the prefix defined
		// in the utils class
		if (!argArray[0].startsWith(CMD_PREFIX))
			return;

		// Extract the "command" part of the first arg out by just ditching the
		// first character
		String commandStr = argArray[0].substring(CMD_PREFIX.length()).toLowerCase();

		// Load the rest of the args in the array into a List for safer access
		List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
		argsList.remove(0); // Remove the command

		try {
			if (superadminCommandMap.containsKey(commandStr))
				superadminCommandMap.get(commandStr).runCommand(event, argsList);
			else if (adminCommandMap.containsKey(commandStr))
				adminCommandMap.get(commandStr).runCommand(event, argsList);
			else if (commandMap.containsKey(commandStr))
				commandMap.get(commandStr).runCommand(event, argsList);
		} catch (CommandFailedException e) {
			AppTools.sendMessage(event.getChannel(), ":negative_squared_cross_mark: " + e.getDenialReason());
		} catch (DiscordException e) {
			AppTools.sendMessage(event.getChannel(), ":negative_squared_cross_mark: Sorry, an error occured while running the command.\n```\n" + e.getErrorMessage() + "\n```");
			System.err.println(e.getErrorMessage());
		}
	}
}
