package es.deusto.ingenieria.ssdd.chat.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import es.deusto.ingenieria.ssdd.chat.serverdata.ServerUser;

public class MainProgram {
	private static int serverPort;
	private static final int PORT = 6789;
	private static ArrayList<ServerUser> connectedUsers;
	private static HashMap<String, String> chats;
	
	public static void main(String[] args) {
		connectedUsers = new ArrayList<ServerUser>();
		chats = new HashMap<>();
		
		serverPort = args.length == 0 ? MainProgram.PORT : Integer.parseInt(args[0]);
		
		try (DatagramSocket udpSocket = new DatagramSocket(serverPort)) {
			DatagramPacket request = null;
			DatagramPacket reply = null;
			byte[] buffer = new byte[1024];
			
			System.out.println(" - Waiting for connections '" + 
			                       udpSocket.getLocalAddress().getHostAddress() + ":" + 
					               serverPort + "' ...");
			
			while (true) {
				request = new DatagramPacket(buffer, buffer.length);
				udpSocket.receive(request);
				reply = processRequest(request);
				
				if (reply != null) {
					udpSocket.send(reply);				
				}
				buffer = new byte[1024];
			}
		} catch (SocketException e) {
			System.err.println("# UDPServer Socket error: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("# UDPServer IO error: " + e.getMessage());
		}
	}

	private static DatagramPacket processRequest(DatagramPacket request) {
		String message = new String(request.getData());
		int op = Integer.valueOf(message.substring(0, 1));
		message = message.substring(1);
		switch (op) {
			case 0:
				return connect(message, request);
	
			case 1:
				disconnect(message, request);
				break;
				
			case 2:
				return openChat(message, request);
			case 3:
				closeChat(message, request);
				break;
			case 4:
				return sendConnectedUsers(request);
			case 6:
				return sendMessage(message, request);
			case 7:
				return receiveMessage(message, request);
			case 8:
				chatOpeningAnswer(message, request);
				break;
		}
		return null;
	}

	private static DatagramPacket connect(String nick, DatagramPacket request) {
		String temp = "false";
		
		for (ServerUser u:connectedUsers) {
			if (u.getNick().equalsIgnoreCase(nick)) {
				temp = "name";
			}
		}
		if (connectedUsers.size() < 10 && temp != "name") {
			ServerUser user = new ServerUser(nick, request.getAddress(), request.getPort());
			connectedUsers.add(user);
			System.out.println(" - " + nick + " connected at "+request.getAddress().getHostAddress()+":"+request.getPort());
			
			temp = "true";
		}
		byte[] buffer = temp.getBytes();
		return new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
	}
	
	private static void disconnect(String nick, DatagramPacket request) {
		ServerUser user = new ServerUser(nick, request.getAddress(), request.getPort());
		connectedUsers.remove(user);
		System.out.println(" - " + nick + " disconnected");
	}
	
	private static DatagramPacket openChat(String message, DatagramPacket request) {
		String[] users = message.split("/");
		if (!chats.containsKey(users[1])) {
			chats.put(users[0].trim(), users[1].trim());
			chats.put(users[1].trim(), users[0].trim());
	
			for (ServerUser u:connectedUsers) {
				if (compareStrings(u.getNick(), users[1])) {
					u.addMessage("5"+users[0]);
				}
			}
		}
		else {
			chatOpeningAnswer("chatting", request);
		}
		return null;
	}
	
	private static void closeChat(String message, DatagramPacket request) {
		String otherUser = chats.get(message.trim());
		chats.remove(otherUser);
		chats.remove(message.trim());
		for (ServerUser u:connectedUsers) {
			if (compareStrings(u.getNick(), otherUser)) {
				u.addMessage("3");
			}
		}
	}
	
	private static DatagramPacket sendConnectedUsers(DatagramPacket request) {
		String list = "";
		for (ServerUser u:connectedUsers) {
			list += u.getNick() + "/n";
		}
		byte[] buffer = list.getBytes();
		return new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
	}
	
	private static DatagramPacket sendMessage(String message, DatagramPacket request) {
		String senderNick = message.substring(0, message.indexOf("/"));
		String text = message.substring(message.indexOf("/")+1, message.length());
		for (ServerUser u:connectedUsers) {
			if (compareStrings(u.getNick(), chats.get(senderNick))) {
				u.addMessage("6"+text);
			}
		}
		return null;
	}
	
	private static DatagramPacket receiveMessage(String message, DatagramPacket request) {
		for (ServerUser u:connectedUsers) {
			if (compareStrings(u.getNick(), message)) {
				if (!u.getMessages().isEmpty()) {
					byte[] buffer = u.pushMessage().getBytes();
					return new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
				}
			}
		}
		String list = "4";
		for (ServerUser u:connectedUsers) {
			list += u.getNick() + "/n";
		}
		byte[] buffer = list.getBytes();
		return new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
	}
	
	private static void chatOpeningAnswer(String message, DatagramPacket request) {
		String senderNick = message.substring(0, message.indexOf("/"));
		String text = message.substring(message.indexOf("/")+1, message.length());
		String to = chats.get(senderNick.trim());
		if (!text.contains("true")) {
			if (!text.contains("chatting")) {
				chats.remove(senderNick.trim());
				chats.remove(to.trim());
			}
		}
		for (ServerUser u:connectedUsers) {
			if (compareStrings(u.getNick(), to)) {
				u.addMessage("8"+text+"/"+to);
			}
		}
	}
	
	private static boolean compareStrings(String a, String b) {
		a = a.trim();
		b = b.trim();
		return a.equalsIgnoreCase(b);
	}
}
