package es.deusto.ingenieria.ssdd.chat.client.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observer;

import javax.swing.Timer;

import es.deusto.ingenieria.ssdd.chat.data.Message;
import es.deusto.ingenieria.ssdd.chat.data.User;
import es.deusto.ingenieria.ssdd.util.observer.local.LocalObservable;

public class ChatClientController {
	private String serverIP;
	private int serverPort;
	private User connectedUser;
	private User chatReceiver;
	private LocalObservable observable;
	private List<String> connectedUsers;
	private Timer t;
	
	public ChatClientController() {
		this.observable = new LocalObservable();
		this.serverIP = null;
		this.serverPort = -1;
		t = new Timer(70, new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				receiveMessage("7"+connectedUser.getNick());
			}
		});
	}
	
	public String getConnectedUser() {
		if (this.connectedUser != null) {
			return this.connectedUser.getNick();
		} else {
			return null;
		}
	}
	
	public String getChatReceiver() {
		if (this.chatReceiver != null) {
			return this.chatReceiver.getNick();
		} else {
			return null;
		}
	}
	
	public String getServerIP() {
		return this.serverIP;
	}
	
	public int gerServerPort() {
		return this.serverPort;
	}
	
	public boolean isConnected() {
		return this.connectedUser != null;
	}
	
	public boolean isChatSessionOpened() {
		return this.chatReceiver != null;
	}
	
	public void addLocalObserver(Observer observer) {
		this.observable.addObserver(observer);
	}
	
	public void deleteLocalObserver(Observer observer) {
		this.observable.deleteObserver(observer);
	}
	
	public int connect(String ip, int port, String nick) {
		this.connectedUser = new User();
		this.connectedUser.setNick(nick);
		this.serverIP = ip;
		this.serverPort = port;
		connectedUsers = new ArrayList<String>();
		
		String message = "0"+connectedUser.getNick();
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP);			
			byte[] byteMsg = message.getBytes();
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
			byte[] buffer = new byte[1024];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			udpSocket.receive(reply);
			String rep = new String(reply.getData());
			if (rep.contains("true")) {
				startListening();
				return 1;
			}
			else if (rep.contains("name")) {
				resetWhenNoConnected();
				return 0;
			}
			else {
				resetWhenNoConnected();
				return -1;
			}
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			resetWhenNoConnected();
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
			resetWhenNoConnected();
			return -1;
		}
	}
	
	private void resetWhenNoConnected() {
		this.connectedUser = null;
		this.serverIP = null;
		this.serverPort = -1;
	}
	
	public boolean disconnect() {
		if (chatReceiver != null) {
			sendChatClosure();
			receiveChatClosure(true);
		}
		String message = "1"+connectedUser.getNick();
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP);			
			byte[] byteMsg = message.getBytes();
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
			return false;
		}
		
		stopListening();
		this.connectedUser = null;
		this.chatReceiver = null;
		connectedUsers.clear();
		connectedUsers = null;
		
		return true;
	}
	
	public List<String> getConnectedUsers() {
		return connectedUsers;
	}
	
	public boolean sendMessage(String message) {
		message = "6"+connectedUser.getNick()+"/"+message;
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP);
			byte[] byteMsg = message.getBytes();
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
		}
		return true;
	}
	
	public void receiveMessage(String message) {	
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP);
			byte[] byteMsg = message.getBytes();
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
			
			byte[] buffer = new byte[10240];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			udpSocket.receive(reply);
			String answer = new String(reply.getData());
			int op = Integer.parseInt(answer.substring(0, 1));
			String content = answer.substring(1);
			
			switch (op) {
				case 3:
					receiveChatClosure(false);
					break;
				case 4:
					connectedUsers.clear();
					if (answer != null) {
						String[] nicks = content.split("/n");
						for (int i = 0; i < nicks.length; i++) {
							connectedUsers.add(nicks[i].trim());	
						}
					}
					break;
				case 5:
					if (!isChatSessionOpened()) {
						String s = "Chat request received from "+content.trim();
						this.observable.notifyObservers(s);
					} else {
						refuseChatRequest("chatting");
					}
					break;
				case 6:
					Message m = new Message();
					m.setTo(connectedUser);
					m.setTimestamp(Calendar.getInstance().getTimeInMillis());
					m.setFrom(chatReceiver);
					m.setText(content);
					this.observable.notifyObservers(m);
					break;
				case 7:
				case 8:
					if (!answer.substring(1).startsWith("true")) {
						chatReceiver = null;
					}
					this.observable.notifyObservers(answer.substring(1));
					break;
				default:
					break;
			}
			
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
		}
	}	
	
	public boolean sendChatRequest(String to) {
		chatReceiver = new User();
		chatReceiver.setNick(to);
		to = "2"+connectedUser.getNick()+"/"+to;
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP);
			byte[] byteMsg = to.getBytes();
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
		}
		return false;
	}	
	
	public void acceptChatRequest(String from) {
		chatReceiver = new User();
		chatReceiver.setNick(from);
		String to = "8"+connectedUser.getNick()+"/true";
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP);
			byte[] byteMsg = to.getBytes();
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
		}
	}
	
	public void refuseChatRequest(String answer) {
		String to = "8"+connectedUser.getNick()+"/"+answer;
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP);
			byte[] byteMsg = to.getBytes();
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
		}
	}	
	
	public boolean sendChatClosure() {
		String to = "3"+connectedUser.getNick();
		try (DatagramSocket udpSocket = new DatagramSocket()) {
			InetAddress serverHost = InetAddress.getByName(serverIP);
			byte[] byteMsg = to.getBytes();
			DatagramPacket request = new DatagramPacket(byteMsg, byteMsg.length, serverHost, serverPort);
			udpSocket.send(request);
		} catch (SocketException e) {
			System.err.println("# UDPClient Socket error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("# UDPClient IO error: " + e.getMessage());
		}
		
		this.chatReceiver = null;
		
		return true;
	}
	
	public void receiveChatClosure(boolean me) {
		this.chatReceiver = null;
		
		String message = "chat_closure";
		message+=String.valueOf(me);
		
		this.observable.notifyObservers(message);
	}
	
	private void startListening() {
		t.start();
	}
	
	private void stopListening() {
		t.stop();
	}
}