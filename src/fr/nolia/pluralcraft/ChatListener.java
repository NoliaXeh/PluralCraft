package fr.nolia.pluralcraft;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.md_5.bungee.api.ChatColor;

public class ChatListener implements Listener{
	
	private void broadcastMessage(Player player, Member member, String message) {

		ChatColor color = Member.colorFromString(member.getColor());
		String name = ChatColor.YELLOW + "<" + color + member.getName()
			+ ChatColor.YELLOW + " | " + player.getName() + ">" + ChatColor.WHITE;
		player.getServer().broadcastMessage(name + " " + message);	
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msg = event.getMessage();
		if (Main.systems.containsKey(player.getName())) {
			PluralSystem system = Main.systems.get(player.getName());
			Member member = system.getMemberFromProxy(msg);
			if (member != null) {
				event.setCancelled(true);
				int i = member.getProxy().length();
				broadcastMessage(player, member, msg.substring(i + 1));
			}
		}
		else if (Main.currentSwitches.containsKey(player.getName()))
		{
			event.setCancelled(true);
			Member member = Main.currentSwitches.get(player.getName());
			broadcastMessage(player, member, msg);
		}
	}
}
