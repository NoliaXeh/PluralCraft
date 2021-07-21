package fr.nolia.pluralcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Main extends JavaPlugin implements Listener {
	
	public static String PC_HELP = ""
			+ "PluralCraft Help:\n"
			+ "  /pc help\n"
			+ "  /pc new <system>\n"
			+ "  /pc member\n"
			+ "  /pc member list\n"
			+ "  /pc member new <member_name>\n"
			+ "  /pc member <member> color <color>\n"
			+ "  /pc member <member> proxy <proxy>\n"
			+ "  /pc member <member> rename <new_name>\n"
			+ "  /pc member <member> remove\n"
			+ "  /pc switch <member>\n";
	
	public static String PC_FILE_NAME = "pc-data.json";
	public static File dataFile;
	public static FileWriter dataFileWriter;
	public static FileReader dataFileReader;
	public static JSONObject dataFileJsonObject;
	public static Map<String, PluralSystem> systems;
	public static Map<String, Member> currentSwitches;
	
	
	@Override
	public void onEnable() {
		System.out.println("PluralCraft: init");
		try {
			dataFile = new File(PC_FILE_NAME);
			System.out.println(dataFile.exists());
			if (!dataFile.exists() && dataFile.createNewFile())
			{
				System.out.println("PluralCraft: No pc-data.json detected. New pc-data.json created.");
				FileWriter tmp = new FileWriter(dataFile);
				tmp.write("{}");
				tmp.flush();
				tmp.close();
			}
			else
				System.out.println("PluralCraft: pc-data.json detected");
//			dataFileWriter = new FileWriter(dataFile);
			dataFileReader = new FileReader(dataFile);
			JSONParser parser = new JSONParser();
			dataFileJsonObject = (JSONObject) parser.parse(dataFileReader);
		} catch (IOException e) {
			System.err.println("PluralCraft: Error opening file pc-data.json");
			e.printStackTrace();
		} catch (ParseException e) {
			System.err.println("PluralCraft: Error parsing file pc-data.json");
			e.printStackTrace();
		}
		BuildSystems(dataFileJsonObject);
		System.out.print("System list size: "); System.out.println(systems.size());
		for (Map.Entry<String, PluralSystem> entry: systems.entrySet()) {
			entry.getValue().print();
		}
		currentSwitches = new HashMap<String, Member>();
		getServer().getPluginManager().registerEvents(new ChatListener(), this);
	}
	
	@Override
	public void onDisable() {
		try {
			if (dataFileWriter != null)
				dataFileWriter.close();
			if (dataFileReader != null)
				dataFileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean BuildSystems(JSONObject raw) {
		systems = new HashMap<>();
		System.out.println("Rebuilding system list");
		System.out.println(raw.size());
		for (Object obj : raw.entrySet()) {
			Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>) obj;
			String name = entry.getKey();
			JSONObject system_raw = entry.getValue();
			System.out.println("System of player " + name);
		    PluralSystem system = new PluralSystem(name);
		    JSONArray members_raw = (JSONArray) system_raw.get("members");
		    for (Object obj2: members_raw) {
		    	JSONObject member_raw = (JSONObject) obj2;
		    	String member_name = (String) member_raw.get("name");
		    	String member_proxy = (String) member_raw.get("proxy");
		    	String member_color = "gold";
		    	if (member_raw.containsKey("color"))
		    		member_color = (String) member_raw.get("color");
				System.out.println("    member " + member_name + "(" + member_proxy + ") added");
		    	Member member = new Member(member_name, member_proxy, member_color);
		    	system.addMember(member);
		    }
		    systems.put(name, system);
		}
		System.out.println("Done");
		return false;
	}
	
	public boolean SaveSystems() {
		JSONObject systemsJson = new JSONObject();
		for (Object obj : systems.entrySet()) {
			Map.Entry<String, PluralSystem> entry = (Map.Entry<String, PluralSystem>) obj;
			String name = entry.getKey();
			PluralSystem system = entry.getValue();
			system.saveToJson(systemsJson);
		}
		String toSave = systemsJson.toJSONString();
		try {
			dataFileWriter = new FileWriter(dataFile);
			dataFileWriter.write(toSave);
			dataFileWriter.flush();
			dataFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean onPC(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))  {
			sender.sendMessage("You can not perform this command from the console. Use /pcc instead");
			return true;
		}
		Player player = (Player) sender;
		
		if (args.length == 0) {
			player.sendMessage(ChatColor.GRAY + PC_HELP);
			return true;
		}
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("new")) {
				return onPCNew(player, cmd, label, args);
			}
			if (args[0].equalsIgnoreCase("member")) {
				return onPCMember(player, cmd, label, args);
			}
			if (args[0].equalsIgnoreCase("switch")) {
				return onPCSwitch(player, cmd, label, args);
			}
			player.sendMessage(ChatColor.GRAY + PC_HELP);
			return true;
		}
		return false;
	}
	
	private boolean onPCNew(Player player, Command cmd, String label, String[] args) {
		player.sendMessage(ChatColor.GREEN + "Creating a new system");
		PluralSystem sys = new PluralSystem(player.getName());
		systems.put(player.getName(), sys);
		SaveSystems();
		return true;
	}

	private boolean onPCMember(Player player, Command cmd, String label, String[] args) {
		// /pc member
		if (args.length == 1) {
			if (currentSwitches.containsKey(player.getName()))
				player.sendMessage(ChatColor.GREEN + "Current member is " + player.getName());
			else if (systems.containsKey(player.getName()))
				player.sendMessage(ChatColor.RED + "No current switches for player " + player.getName());
			else
				player.sendMessage(ChatColor.RED + "No system found for player " + player.getName());
			return true;
		}
		
		if (args.length >= 2) {
			if (args[1].equalsIgnoreCase("list")) {
				return onPCMemberList(player, cmd, label, args);
			}
			if (args[1].equalsIgnoreCase("new")) {
				return onPCMemberNew(player, cmd, label, args);
			}
			return onPCMemberSpecific(player, cmd, label, args);
		}
		return false;
	}
	
	private boolean onPCMemberNew(Player player, Command cmd, String label, String[] args) {
		if (args.length == 2) {
			player.sendMessage(ChatColor.RED + "Missing argument <member_name>");
			return true;
		}
		if (!systems.containsKey(player.getName())) {
			player.sendMessage(ChatColor.RED + "You need to create a system first");
			return true;
		}
		String memberName = args[2];
		PluralSystem system = systems.get(player.getName());
		for (Member member: system.getMembers()) {
			if (member.getName().equals(memberName)) {
				player.sendMessage(ChatColor.RED + "This system already has a member named " + memberName);
				return true;
			}
		}
		Member newMember = new Member(memberName);
		system.addMember(newMember);
		player.sendMessage(ChatColor.GREEN + "New member " + memberName + " created");
		System.out.println("PluralCraft: player " + player.getName() + " added \"" + memberName +  "\" member to their system");
		SaveSystems();
		return true;
	}

	private boolean onPCMemberSpecific(Player player, Command cmd, String label, String[] args) {
		String memberName = args[1];
		if (args.length == 2) {
			player.sendMessage(ChatColor.RED + "Missing argument <option>");
			return true;
		}
		if (!systems.containsKey(player.getName())) {
			player.sendMessage(ChatColor.RED + "You need to create a system first");
			return true;
		}
		PluralSystem system = systems.get(player.getName());
		if (!system.hasMember(memberName)) {
			player.sendMessage(ChatColor.RED + "No member named " + memberName + " found."
					+ " /pc member list for a list of registered members");
			return true;
		}
		Member member = system.getMember(memberName);
		if (args[2].equalsIgnoreCase("color")) {
			if (args.length != 4) {
				player.sendMessage(ChatColor.RED + "Missing argument <color>");
				return true;
			}
			String color = args[3];
			ChatColor test = Member.colorFromString(color);
			if (test.equals(ChatColor.RESET)) {
				player.sendMessage(ChatColor.RED + "This color does not exist. Chose one of the following: "
						+ ChatColor.BLACK + "black "
						+ ChatColor.DARK_BLUE + "dark_blue "
						+ ChatColor.DARK_GREEN + "dark_green "
						+ ChatColor.DARK_AQUA + "dark_aqua "
						+ ChatColor.DARK_RED + "dark_red "
						+ ChatColor.DARK_PURPLE + "dark_purple "
						+ ChatColor.GOLD + "gold "
						+ ChatColor.GRAY + "gray "
						+ ChatColor.DARK_GRAY + "dark_gray "
						+ ChatColor.BLUE + "blue "
						+ ChatColor.GREEN + "green "
						+ ChatColor.AQUA + "aqua "
						+ ChatColor.RED + "red "
						+ ChatColor.LIGHT_PURPLE +"light_purple "
						+ ChatColor.YELLOW + "yellow "
						+ ChatColor.WHITE + "white ");
				return true;
			}
			member.setColor(color);
			SaveSystems();
			player.sendMessage(ChatColor.GREEN + "Set color " + test + color
					+ ChatColor.GREEN + " for member " + test + memberName);
			return true;
		}
		if (args[2].equalsIgnoreCase("rename")) {
			if (args.length != 4) {
				player.sendMessage(ChatColor.RED + "Missing argument <new_member_name>");
				return true;
			}
			String newName = args[3];
			member.setName(newName);
			SaveSystems();
			player.sendMessage(ChatColor.GREEN + "Member "  + member.getChatColor() + memberName
					+ ChatColor.GREEN + " renamed to " + member.getChatColor() + member.getName());
			return true;
		}
		if (args[2].equalsIgnoreCase("remove")) {
			system.removeMember(member);
			SaveSystems();
			player.sendMessage(ChatColor.GREEN + "Member "  + member.getChatColor() + memberName
					+ ChatColor.GREEN + " removed");
			if (currentSwitches.containsKey(player.getName())
					&& currentSwitches.get(player.getName()).getName().equals(memberName)) {
				currentSwitches.remove(player.getName());
			}
			return true;
		}
		if (args[2].equalsIgnoreCase("proxy")) {
			if (args.length != 4) {
				player.sendMessage(ChatColor.RED + "Missing argument <proxy>");
				return true;
			}
			String proxy = args[3];
			member.setProxy(proxy);
			SaveSystems();
			player.sendMessage(ChatColor.GREEN + "Set new proxy " + proxy + " for member "  + member.getChatColor() + memberName);
			return true;
		}
		return false;
	}

	private boolean onPCMemberList(Player player, Command cmd, String label, String[] args) {
		if (!systems.containsKey(player.getName())) {
			player.sendMessage(ChatColor.RED + "No system found for player " + player.getName());
		}
		PluralSystem system = systems.get(player.getName());
		String result = ChatColor.GRAY +  "System for player " + player.getName() +
				" has " + String.valueOf(system.getMembersCount()) + 
				" registered members:\n";
		
		for (Member member: system.getMembers()) {
			result += " - " + member.getName() + " (" + member.getProxy() + ")";
			if (currentSwitches.containsKey(player.getName())
					&& currentSwitches.get(player.getName()).getName().equals(member.getName())) {
				result += ChatColor.GOLD + " ** current front **" + ChatColor.GRAY ;
			}
			result += "\n";
		}
		
		player.sendMessage(result);
		return false;
	}

	private boolean onPCSwitch(Player player, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			player.sendMessage(ChatColor.GREEN + "Reseting switch...");
			if (currentSwitches.containsKey(player.getName())) {
				currentSwitches.remove(player.getName());
			}
		}
		else if (args.length >= 2) {
			String memberName = args[1];
			if (!systems.containsKey(player.getName())) {
				player.sendMessage(ChatColor.RED + "No system registered for player " + player.getName());
				return true;
			}
			PluralSystem playerSystem = systems.get(player.getName());
			for (Member member: playerSystem.getMembers()) {
				if (member.getName().equals(memberName)) {
					currentSwitches.put(player.getName(), member);
					player.sendMessage(ChatColor.GREEN + "Switching player to " + member.getName());
					return true;
				}
			}
			player.sendMessage(ChatColor.RED + "No member named " + memberName + " found... \"/pc member list\" to list members");
			return true;
		}
		return false;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (label.equalsIgnoreCase("pc")) {
			return onPC(sender, cmd, label, args);
		}
			
		return false;
	}
	
}
