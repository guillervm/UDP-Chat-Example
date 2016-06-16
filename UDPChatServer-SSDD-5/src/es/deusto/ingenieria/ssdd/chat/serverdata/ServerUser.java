package es.deusto.ingenieria.ssdd.chat.serverdata;

import java.net.InetAddress;
import java.util.ArrayList;

import es.deusto.ingenieria.ssdd.chat.data.User;

public class ServerUser extends User {
	private InetAddress ip;
	private int port;
	private ArrayList<String> messages;
	
	public ServerUser(String nick, InetAddress ip, int port) {
		this.setNick(nick);
		this.setIp(ip);
		this.setPort(port);
		messages = new ArrayList<String>();
	}
	
	public InetAddress getIp() {
		return ip;
	}
	
	public void setIp(InetAddress ip) {
		this.ip = ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public ArrayList<String> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<String> messages) {
		this.messages = messages;
	}
	
	public void addMessage(String message) {
		messages.add(message);
	}
	
	public String pushMessage() {
		String t = messages.get(0);
		messages.remove(0);
		return t;
	}
}
