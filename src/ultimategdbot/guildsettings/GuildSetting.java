package ultimategdbot.guildsettings;

import sx.blah.discord.handle.obj.IGuild;
import ultimategdbot.exceptions.InvalidValueException;

public abstract class GuildSetting<T> {
	
	protected String info;
	protected IGuild guild;
	protected T value;

	public GuildSetting(IGuild guild, String info, T value) {
		this.info = info;
		this.guild = guild;
		
		if (value != null)
			this.value = value;
		else
			this.value = defaultValue();
	}
	
	public String getInfo() {
		return info;
	}

	public IGuild getGuild() {
		return guild;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
	public void setParsedValue(String valueStr) throws InvalidValueException {
		setValue(parseValue(valueStr));
	}
	
	public T defaultValue() {
		return null;
	}
	
	public void resetValue() {
		setValue(defaultValue());
	}
	
	/**
	 * This method will construct the value from the given String, and the
	 * result will be returned
	 * 
	 * @param valueStr
	 *            - the String to parse as value
	 * @throws InvalidValueException
	 *             if the method failed to parse the value as a valid one
	 * @return the parsed value
	 */
	public abstract T parseValue(String valueStr) throws InvalidValueException;

	@Override
	public String toString() {
		String s = this.getClass().getSimpleName().replaceAll("Setting", "");
		String res = "";
		int i = 0;
		
		for (char c : s.toCharArray()) {
			if (i > 0 && (c + "").matches("[A-Z]"))
				res += "_";
			res += c;
			i++;
		}
		
		return res.toLowerCase();
	}
	
	public String valueToString() {
		return value != null ? value.toString() : "N/A";
	}
}
