package ultimategdbot.gdevents.listeners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import ultimategdbot.exceptions.RawDataMalformedException;
import ultimategdbot.gdevents.GDEvent;
import ultimategdbot.gdevents.handler.GDEventHandler;
import ultimategdbot.gdevents.levels.LastAwardedDeletedGDEvent;
import ultimategdbot.gdevents.levels.LastAwardedStateChangedGDEvent;
import ultimategdbot.gdevents.levels.NewAwardedGDEvent;
import ultimategdbot.guildsettings.ChannelAwardedLevelsSetting;
import ultimategdbot.guildsettings.RoleAwardedLevelsSetting;
import ultimategdbot.net.database.dao.impl.DAOFactory;
import ultimategdbot.net.database.entities.GuildSettings;
import ultimategdbot.net.database.entities.UserSettings;
import ultimategdbot.net.geometrydash.GDLevel;
import ultimategdbot.net.geometrydash.GDUser;
import ultimategdbot.net.geometrydash.GDUserFactory;
import ultimategdbot.util.AppTools;
import ultimategdbot.util.GDUtils;

/**
 * Utility class to manage event listeners related to levels in the Awarded section
 * of Geometry Dash.
 * 
 * @author Alex1304
 *
 */
public abstract class AwardedLevelListeners {
	
	/**
	 * Contains the notification messages sent for the latest awarded level.
	 */
	private static List<IMessage> notifMessageOfLastAwardedForEachGuild = new ArrayList<>();
	
	/**
	 * Builds a list of listeners and returns it
	 * @return a list of GD event listeners.
	 */
	public static List<GDEventHandler<? extends GDEvent>> getListeners() {
		List<GDEventHandler<? extends GDEvent>> listeners = new ArrayList<>();
		
		// Code executed when a new level gets rated on Geometry Dash.
		listeners.add(new GDEventHandler<>((NewAwardedGDEvent event) -> {
			event.getLevelList().forEach(level -> {
				notifySubscribers("A new level has just been rated on Geometry Dash!!!", level,
						newAwardedLevelEmbed(level));
				DAOFactory.getAwardedLevelDAO().insert(level);
			});
		}));
		
		// Code executed when the last known level gets unrated from Geometry Dash
		listeners.add(new GDEventHandler<>((LastAwardedDeletedGDEvent event) -> {
			notifySubscribers("A level just got un-rated from Geometry Dash...", event.getLevel(),
					unratedLevelEmbed(event.getLevel()));
			DAOFactory.getAwardedLevelDAO().delete(event.getLevel());
		}));

		// Code executed when the last known level experiences a state change on Geometry Dash
		listeners.add(new GDEventHandler<>((LastAwardedStateChangedGDEvent event) -> {
			for (IMessage message : notifMessageOfLastAwardedForEachGuild)
				if (message != null)
					AppTools.editMessage(message, message.getContent(), newAwardedLevelEmbed(event.getLevel()));
		}));
		
		return listeners;
	}
	
	/**
	 * Sends a message to all guilds in the channel specified in the "channel
	 * awarded levels" guild setting.
	 * 
	 * @param message
	 *            - the message to send
	 * @param level
	 *            - the newly awarded level it's supposed to announce
	 * @param levelEmbed
	 *            - the embed containing level info
	 */
	private static void notifySubscribers(String message, GDLevel level, EmbedObject levelEmbed) {
		List<GuildSettings> gsList = DAOFactory.getGuildSettingsDAO().findAllWithChannelAwardedLevelsSetup();
		notifMessageOfLastAwardedForEachGuild.clear();

		for (GuildSettings gs : gsList) {
			IGuild guild = gs.getGuild();

			if (guild != null) {
				IChannel channelAwardedLevels = gs.getSetting(ChannelAwardedLevelsSetting.class).getValue();
				IRole roleAwardedLevelsSub = gs.getSetting(RoleAwardedLevelsSetting.class).getValue();
				IMessage lastMessage = null;
				if (channelAwardedLevels != null) {
					lastMessage = AppTools.sendMessage(channelAwardedLevels,
							(roleAwardedLevelsSub != null ? roleAwardedLevelsSub.mention() + " " : "")
									+ message,
							levelEmbed);
					
					try {
						GDUser creator = GDUserFactory.buildGDUserFromNameOrDiscordTag(level.getCreator());
						if (creator != null) {
							UserSettings us = DAOFactory.getUserSettingsDAO().findByGDUserID(creator.getAccountID());
							if (us != null) {
								IUser discordUser = guild.getUserByID(us.getUserID());
								if (discordUser != null) {
									AppTools.sendMessage(channelAwardedLevels, "Congratulations " + discordUser.mention()
											+ " for getting your level rated !");
								}
							}
						}
					} catch (RawDataMalformedException | IOException e) {
						AppTools.sendDebugPMToSuperadmin("Failed to announce Awarded level to user: " + e.getLocalizedMessage());
						e.printStackTrace();
					}
				}
				notifMessageOfLastAwardedForEachGuild.add(lastMessage);
			} else {
				System.err.println("[INFO] Guild deleted");
				DAOFactory.getGuildSettingsDAO().delete(gs);
			}
		}
	}
	
	/**
	 * Constructs the announcement embed for the new awarded level
	 * 
	 * @param level - the new awarded level
	 * @return the embed that will be sent to guilds
	 */
	private static EmbedObject newAwardedLevelEmbed(GDLevel level) {
		return GDUtils.buildEmbedForGDLevel("New rated level!", "https://i.imgur.com/asoMj1W.png", level);
	}
	
	/**
	 * Constructs the announcement embed for the unrated level
	 * 
	 * @param level - the unrated level
	 * @return the embed that will be sent to guilds
	 */
	private static EmbedObject unratedLevelEmbed(GDLevel level) {
		return GDUtils.buildEmbedForGDLevel("Level unrated...", "https://i.imgur.com/fPECXUz.png", level);
	}
}
