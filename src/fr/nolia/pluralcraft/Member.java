package fr.nolia.pluralcraft;

import org.json.simple.JSONObject;

import net.md_5.bungee.api.ChatColor;

public class Member {
	private String name;
	private String proxy;
	private String color;
	
	public static ChatColor colorFromString(String raw) {
		if (raw.equals("black"))
		    return ChatColor.BLACK;
		if (raw.equals("dark_blue"))
		    return ChatColor.DARK_BLUE;
		if (raw.equals("dark_green"))
		    return ChatColor.DARK_GREEN;
		if (raw.equals("dark_aqua"))
		    return ChatColor.DARK_AQUA;
		if (raw.equals("dark_red"))
		    return ChatColor.DARK_RED;
		if (raw.equals("dark_purple"))
		    return ChatColor.DARK_PURPLE;
		if (raw.equals("gold"))
		    return ChatColor.GOLD;
		if (raw.equals("gray"))
		    return ChatColor.GRAY;
		if (raw.equals("dark_gray"))
		    return ChatColor.DARK_GRAY;
		if (raw.equals("blue"))
		    return ChatColor.BLUE;
		if (raw.equals("green"))
		    return ChatColor.GREEN;
		if (raw.equals("aqua"))
		    return ChatColor.AQUA;
		if (raw.equals("red"))
		    return ChatColor.RED;
		if (raw.equals("light_purple"))
		    return ChatColor.LIGHT_PURPLE;
		if (raw.equals("yellow"))
		    return ChatColor.YELLOW;
		if (raw.equals("white"))
		    return ChatColor.WHITE;
		return ChatColor.RESET;
	}
	
	public Member (String name) {
		this.name = name;
		this.proxy = name + ":";
		this.color = "gold";
	}
	
	public Member (String name, String proxy) {
		this.name = name;
		this.proxy = proxy;
		this.color = "gold";
	}
	public Member (String name, String proxy, String color) {
		this.name = name;
		this.proxy = proxy;
		this.color = color;
	}
	
	public String getName() {
		return name;
	}
	
	public String getProxy() {
		return proxy;
	}
	
	public String getColor() {
		return color;
	}
	
	public ChatColor getChatColor() {
		return colorFromString(color);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	
	public JSONObject getJSON() {
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("proxy", proxy);
		obj.put("color", color);
		return obj;
	}
}
