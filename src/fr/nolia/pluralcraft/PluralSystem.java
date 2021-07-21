package fr.nolia.pluralcraft;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PluralSystem {
	private ArrayList<Member> members;
	private String player;
	
	public PluralSystem(String player) {
		this.player = player;
		this.members = new ArrayList<Member>();
	}
	
	public boolean addMember(Member member) {
		members.add(member);
		return true;
	}
	
	public boolean removeMember(Member member) {
		members.remove(member);
		return true;
	}
	
	public ArrayList<Member> getMembers() {
		return members;
	}
	
	public int getMembersCount() {
		return members.size();
	}
	
	public String getPlayer() {
		return player;
	}
	
	public boolean hasMember(String memberName) {
		for (Member member: members)
			if (member.getName().equalsIgnoreCase(memberName))
				return true;
		return false;
	}
	
	public Member getMemberFromProxy(String proxiedMessage) {
		for (Member member: members)
		{
			String proxy = member.getProxy() + " ";
			int proxyLength = proxy.length();
			if (proxiedMessage.length() < proxyLength)
				continue;
			String beggining = proxiedMessage.substring(0, proxyLength);
			if (beggining.equals(proxy))
				return member;
		}
		return null;
	}
	
	public Member getMember(String memberName) {
		for (Member member: members)
			if (member.getName().equalsIgnoreCase(memberName))
				return member;
		return null;
	}
	
	public boolean saveToJson(JSONObject json) {
		JSONObject self = new JSONObject();
		JSONArray selfMembers = new JSONArray();
		for (Member member: members) {
			JSONObject singleMember = member.getJSON();
			selfMembers.add(singleMember);
		}
		self.put("members", selfMembers);
		self.put("player", player);
		json.put(player, self);
		return true;
	}
	
	public void print() {
		System.out.println("System of player " + player);
		String result = "";
		for (Member member: members) {
			result += member.getName() + " (" + member.getProxy() + ")";
			result += "\n";
		}
		System.out.println(result);
	}
}
