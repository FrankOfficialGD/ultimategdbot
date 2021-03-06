package ultimategdbot.commands.impl;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import ultimategdbot.app.Main;
import ultimategdbot.commands.Command;
import ultimategdbot.commands.CoreCommand;
import ultimategdbot.exceptions.CommandFailedException;
import ultimategdbot.exceptions.RawDataMalformedException;
import ultimategdbot.gdevents.users.UserModdedGDEvent;
import ultimategdbot.gdevents.users.UserUnmoddedGDEvent;
import ultimategdbot.net.database.dao.impl.DAOFactory;
import ultimategdbot.net.database.dao.impl.GDModlistDAO;
import ultimategdbot.net.database.dao.impl.UserSettingsDAO;
import ultimategdbot.net.database.entities.UserSettings;
import ultimategdbot.net.geometrydash.GDRole;
import ultimategdbot.net.geometrydash.GDServer;
import ultimategdbot.net.geometrydash.GDUser;
import ultimategdbot.net.geometrydash.GDUserFactory;
import ultimategdbot.util.AppTools;
import ultimategdbot.util.BotRoles;
import ultimategdbot.util.Emoji;

/**
 * Allows the user to check Moderator access for a specific Geometry Dash account.
 * The command can be run without arguments if the user is linked to a GD accout ;
 * it will then check mod access for the GD account he is linked to.
 * 
 * @author Alex1304
 *
 */
public class CheckModCommand extends CoreCommand {

	public CheckModCommand(EnumSet<BotRoles> rolesRequired) {
		super("checkmod", rolesRequired);
	}

	@Override
	public void runCommand(MessageReceivedEvent event, List<String> args) throws CommandFailedException {
		try {
			GDUser user;
			if (args.isEmpty()) {
				UserSettings us = new UserSettingsDAO().find(event.getAuthor().getLongID());
				if (us == null || !us.isLinkActivated())
					throw new CommandFailedException("You are not yet linked to any Geometry Dash account.");
				user = GDUserFactory.buildGDUserFromProfileRawData(GDServer.fetchUserProfile(us.getGdUserID()));
			} else
				user = GDUserFactory.buildGDUserFromNameOrDiscordTag(AppTools.concatCommandArgs(args));
			
			if (user == null)
				throw new CommandFailedException("This user isn't linked to any Geometry Dash account.");
			
			GDModlistDAO gdmldao = DAOFactory.getGDModlistDAO();
			
			String response = "__Checking mod access for user **" + user.getName() + "**:__\n";
			if (user.getRole() == GDRole.USER) {
				response += Emoji.FAILED + " Failed. Nothing found.";
				if (gdmldao.find(user.getAccountID()) != null) {
					gdmldao.delete(user);
					Main.GD_EVENT_DISPATCHER.dispatch(new UserUnmoddedGDEvent(user));
				}
			}
			else {
				response += Emoji.SUCCESS + " Success! Access granted : " + user.getRole().toString();
				if (gdmldao.find(user.getAccountID()) == null) {
					gdmldao.insert(user);
					Main.GD_EVENT_DISPATCHER.dispatch(new UserModdedGDEvent(user));
				}
			}
			
			AppTools.sendMessage(event.getChannel(), response);
		} catch (IOException e) {
			throw new CommandFailedException("Unable to communicate with Geometry Dash servers at the moment. "
					+ "Please try again later.");
		} catch (RawDataMalformedException e) {
			throw new CommandFailedException("This user couldn't be found on Geometry Dash servers. "
					+ "If the user you're looking for has a nickname containing spaces, you can replace them with "
					+ "underscores `_`");
		}
	}

	@Override
	public String getHelp() {
		return "Tells you whether the user specified is granted Geometry Dash Moderator.\n"
				+ "Note that this command doesn't act like the \"req\" button: it does just "
				+ "check for the presence of the Moderator badge on the user's profile.\n"
				+ "You can provide the target user either by giving his username, his playerID,"
				+ " mentionning someone who linked his GD account, or without argument to test "
				+ "it on yourself.";
	}

	@Override
	public String[] getSyntax() {
		String[] syn = { "", "<@mention>", "<username_or_playerID>" };
		return syn;
	}

	@Override
	public String[] getExamples() {
		String[] ex = { "", "@" + AppTools.formatDiscordUsername(Main.DISCORD_ENV.getSuperadmin()), "ViPriN", "71" };
		return ex;
	}

	@Override
	protected Map<String, Command> initSubCommandMap() {
		return null;
	}

}
