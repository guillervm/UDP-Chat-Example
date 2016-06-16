package es.deusto.ingenieria.ssdd.chat.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import es.deusto.ingenieria.ssdd.chat.client.controller.ChatClientController;
import es.deusto.ingenieria.ssdd.chat.data.Message;

public class JFrameMainWindow extends JFrame implements Observer {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtFieldServerIP;
	private JTextField txtFieldServerPort;
	private JTextField txtFieldNick;
	private JButton btnConnect;
	private JList<String> listUsers;
	private JTextPane textAreaHistory;
	private JTextArea textAreaSendMsg;
	private JButton btnSendMsg;
	private SimpleDateFormat textFormatter = new SimpleDateFormat("HH:mm:ss");
	
	private ChatClientController controller;
	private Timer refresh;

	/**
	 * Create the frame.
	 */
	public JFrameMainWindow(final ChatClientController controller) {
		this.controller = controller;
		//Add the frame as Observer of the Controller
		this.controller.addLocalObserver(this);
		
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowIconified(WindowEvent e) {}
			
			@Override
			public void windowDeiconified(WindowEvent e) {}
			
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				if (controller.isConnected()) {
					controller.disconnect();
				}
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});
		
		setResizable(false);
		setType(Type.UTILITY);
		setTitle("Chat main window");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panelUsers = new JPanel();
		panelUsers.setBorder(new TitledBorder(null, "Connected users", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(panelUsers, BorderLayout.EAST);
		panelUsers.setLayout(new BorderLayout(0, 0));
		panelUsers.setPreferredSize(new Dimension(200, 0));
		panelUsers.setMinimumSize(new Dimension(200, 0));
		
		listUsers = new JList<>();
		listUsers.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listUsers.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		listUsers.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				selectUser();
			}			
		});
		
		panelUsers.add(listUsers);
		
		JPanel panelConnect = new JPanel();
		panelConnect.setBorder(new TitledBorder(null, "Connection details", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(panelConnect, BorderLayout.NORTH);
		
		JLabel lblServerIp = new JLabel("Server IP:");		
		JLabel lblServerPort = new JLabel("Server Port:");
		
		txtFieldServerIP = new JTextField();
		txtFieldServerIP.setColumns(10);
		txtFieldServerPort = new JTextField();
		txtFieldServerPort.setColumns(10);
		
		JLabel lblNick = new JLabel("Nick:");
		
		txtFieldNick = new JTextField();
		txtFieldNick.setColumns(10);
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnConnectClick();
			}
		});
		btnConnect.setToolTipText("Connect");
		GroupLayout gl_panelConnect = new GroupLayout(panelConnect);
		gl_panelConnect.setHorizontalGroup(
			gl_panelConnect.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelConnect.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelConnect.createParallelGroup(Alignment.LEADING)
						.addComponent(lblServerIp)
						.addComponent(lblServerPort))
					.addGap(21)
					.addGroup(gl_panelConnect.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelConnect.createSequentialGroup()
							.addComponent(txtFieldServerIP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(37)
							.addComponent(lblNick)
							.addGap(18)
							.addComponent(txtFieldNick, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
							.addGap(28)
							.addComponent(btnConnect))
						.addComponent(txtFieldServerPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(302, Short.MAX_VALUE))
		);
		gl_panelConnect.setVerticalGroup(
			gl_panelConnect.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelConnect.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelConnect.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblServerIp)
						.addComponent(txtFieldServerIP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNick)
						.addComponent(btnConnect)
						.addComponent(txtFieldNick, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelConnect.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblServerPort)
						.addComponent(txtFieldServerPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(15, Short.MAX_VALUE))
		);
		panelConnect.setLayout(gl_panelConnect);
		
		JPanel panelHistory = new JPanel();
		panelHistory.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "History", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(panelHistory, BorderLayout.CENTER);
		panelHistory.setLayout(new BorderLayout(0, 0));
		
		textAreaHistory = new JTextPane();
		textAreaHistory.setBackground(Color.BLACK);
		textAreaHistory.setToolTipText("Messages history");
		textAreaHistory.setEditable(false);
		
		JScrollPane scrollPaneHistory = new JScrollPane(textAreaHistory);
		scrollPaneHistory.setViewportBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelHistory.add(scrollPaneHistory);
		
		JPanel panelSendMsg = new JPanel();
		contentPane.add(panelSendMsg, BorderLayout.SOUTH);
		panelSendMsg.setBorder(new TitledBorder(null, "New message", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelSendMsg.setLayout(new BorderLayout(0, 0));
		
		btnSendMsg = new JButton("Send");
		btnSendMsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnSendClick();
			}
		});
		btnSendMsg.setToolTipText("Send new message");
		btnSendMsg.setEnabled(false);
		panelSendMsg.add(btnSendMsg, BorderLayout.EAST);
		
		textAreaSendMsg = new JTextArea();
		textAreaSendMsg.setTabSize(3);
		textAreaSendMsg.setRows(4);
		textAreaSendMsg.setToolTipText("New message");	
		
		JScrollPane scrollPaneNewMsg = new JScrollPane(textAreaSendMsg);
		scrollPaneNewMsg.setViewportBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelSendMsg.add(scrollPaneNewMsg, BorderLayout.CENTER);
		
		refresh = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshContacts();
			}
		});
		
		//TODO provisional, quitar para que los campos salgan vacios
		txtFieldServerIP.setText("127.0.0.1");
		txtFieldServerPort.setText("6789");
		txtFieldNick.setText("guillervm");
	}
	
	private void refreshContacts() {
		DefaultListModel<String> listModel = new DefaultListModel<>();
		
		for (String user : controller.getConnectedUsers()) {
			listModel.addElement(user);
		}
		
		this.listUsers.setModel(listModel);
	}
	
	private void btnConnectClick() {
		if (!this.controller.isConnected()) {
			if (this.txtFieldServerIP.getText().trim().isEmpty() ||
				this.txtFieldServerPort.getText().trim().isEmpty() ||
				this.txtFieldNick.getText().trim().isEmpty() ) {				
				JOptionPane.showMessageDialog(this, "Some connection parameters are empty", "Connection initializarion error", JOptionPane.ERROR_MESSAGE);				
				
				return;
			}
			if (this.txtFieldNick.getText().trim().contains("/")) {
				JOptionPane.showMessageDialog(this, "Nickname can't content '/' character", "Connection initializarion error", JOptionPane.ERROR_MESSAGE);				
				
				return;
			}
			if (this.txtFieldNick.getText().trim().equalsIgnoreCase("false")) {
				JOptionPane.showMessageDialog(this, "Nickname 'false' not allowed", "Connection initializarion error", JOptionPane.ERROR_MESSAGE);				
				
				return;
			}
			
			//Connect to the server
			int error = this.controller.connect(this.txtFieldServerIP.getText(), Integer.parseInt(this.txtFieldServerPort.getText()), this.txtFieldNick.getText());
			if (error == 1) {
				
				//Obtain the list of connected Users
				List<String> connectedUsers = this.controller.getConnectedUsers();
				DefaultListModel<String> listModel = new DefaultListModel<>();
				
				for (String user : connectedUsers) {
					listModel.addElement(user);
				}
				
				this.listUsers.setModel(listModel);
				
				this.txtFieldServerIP.setEditable(false);
				this.txtFieldServerPort.setEditable(false);
				this.txtFieldNick.setEditable(false);
				this.btnConnect.setText("Disconnect");
				this.textAreaHistory.setText("");
				this.textAreaSendMsg.setText("");
				
				this.setTitle("Chat main window - 'Connected'");
				refresh.start();
			} else {
				if (error == -1) {
					JOptionPane.showMessageDialog(this, "Can't connect to the server.", "Connection error", JOptionPane.ERROR_MESSAGE);
				}
				else {
					JOptionPane.showMessageDialog(this, "Nick already in use.", "Connection error", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			//Disconnect from the server
			if (this.controller.disconnect()) {
				this.txtFieldServerIP.setEditable(true);
				this.txtFieldServerPort.setEditable(true);
				this.txtFieldNick.setEditable(true);
				this.listUsers.setEnabled(true);
				this.listUsers.clearSelection();
				this.btnConnect.setText("Connect");
				this.btnSendMsg.setEnabled(false);
				this.textAreaHistory.setText("");
				this.textAreaSendMsg.setText("");
				DefaultListModel<String> listModel = new DefaultListModel<>();
				this.listUsers.setModel(listModel);
				
				this.setTitle("Chat main window - 'Disconnected'");
				refresh.stop();
			} else {
				JOptionPane.showMessageDialog(this, "Disconnection from the server fails.", "Disconnection error", JOptionPane.ERROR_MESSAGE);				
			}
		}
	}
	
	private void selectUser() {
		if (this.listUsers.getSelectedIndex() != -1 && this.controller.getConnectedUser() != null) {		
			//Send chat Request
			if (!this.controller.isChatSessionOpened()) {			
				String to = this.listUsers.getSelectedValue().trim();
				int result = JOptionPane.showConfirmDialog(this, "Do you want to start a new chat session with '" + to + "'", "Open chat Session", JOptionPane.YES_NO_OPTION);

				if (result == JOptionPane.OK_OPTION && this.controller.sendChatRequest(to)) {
					this.setTitle("Chat session between '" + this.controller.getConnectedUser() + "' & '" + to + "'");
				} else {
					this.listUsers.clearSelection();
				}
			//Send a chat closure
			} else if (this.controller.isChatSessionOpened() && this.listUsers.getSelectedValue().equals(this.controller.getChatReceiver())) {			
				int result = JOptionPane.showConfirmDialog(this, "Do you want to close your current chat session with '" + this.controller.getChatReceiver().trim() + "'", "Close chat Session", JOptionPane.YES_NO_OPTION);				

				if (result == JOptionPane.OK_OPTION && this.controller.sendChatClosure()) {
					this.listUsers.clearSelection();					
					this.setTitle("Chat main window - 'Connected'");
				}
			}
		}
	}
	
	private void btnSendClick() {
		if (!this.textAreaSendMsg.getText().trim().isEmpty()) {
		
			if (controller.getChatReceiver() == null) {				
				JOptionPane.showMessageDialog(this, "You haven't select a destination user", "Chat initialization error", JOptionPane.ERROR_MESSAGE);				
				
				return;
			}			
			
			String message = this.textAreaSendMsg.getText().trim();
			
			if (this.controller.sendMessage(message)) {
				this.appendSentMessageToHistory();
				this.textAreaSendMsg.setText("");
			} else {
				JOptionPane.showMessageDialog(this, "Message can't be delivered.", "Error sending a message", JOptionPane.ERROR_MESSAGE);				
			}
		}
	}
	
	private void appendSentMessageToHistory() {		
		String time = textFormatter.format(GregorianCalendar.getInstance().getTime());		
		String newMessage = " " + time + " - [" + this.controller.getConnectedUser() + "]: " + this.textAreaSendMsg.getText() + "\n";
		SimpleAttributeSet attrs = new SimpleAttributeSet();
		StyleConstants.setBold(attrs, true);
		StyleConstants.setForeground(attrs, Color.WHITE);
		
		try {
			this.textAreaHistory.getStyledDocument().insertString(this.textAreaHistory.getStyledDocument().getLength(), newMessage, attrs);
		} catch (BadLocationException e) {
			System.err.println("# Error updating message history: " + e.getMessage());
		} 
	}
	
	private void appendReceivedMessageToHistory(String message, String user, long timestamp) {		
		String time = textFormatter.format(new Date(timestamp));		
		String newMessage = " " + time + " - [" + user + "]: " + message.trim() + "\n";
		SimpleAttributeSet attrs = new SimpleAttributeSet();
		StyleConstants.setBold(attrs, true);
		StyleConstants.setForeground(attrs, Color.MAGENTA);		
		
		try {
			this.textAreaHistory.getStyledDocument().insertString(this.textAreaHistory.getStyledDocument().getLength(), newMessage, attrs);
		} catch (BadLocationException e) {
			System.err.println("# Error updating message history: " + e.getMessage());
		} 
	}

	@Override
	public void update(Observable observable, Object object) {
		
		//Update this method to process the request received from other users
		
		if (this.controller.isConnected()) {			
			if (object.getClass().equals(Message.class)) {
				Message newMessage = (Message) object;
				if (newMessage.getTo().getNick() == this.controller.getConnectedUser()) {
					this.appendReceivedMessageToHistory(newMessage.getText(), newMessage.getFrom().getNick(), newMessage.getTimestamp());
				}
			} else {
				String o = (String)object;
				if (o.startsWith("true")){
					this.setTitle("Chat session between '" + this.controller.getConnectedUser() + "' & '" + controller.getChatReceiver() + "'");
					btnSendMsg.setEnabled(true);
				} else if (o.startsWith("refused")) {
					JOptionPane.showMessageDialog(this, "Your chat request has been refused.", "Chat opening error", JOptionPane.ERROR_MESSAGE);
				} else if (o.startsWith("chatting")) {
					JOptionPane.showMessageDialog(this, "The other person is already chatting.", "Chat opening error", JOptionPane.ERROR_MESSAGE);
				} else if (o.startsWith("chat_closure")) {
					setTitle("Chat main window");
					btnSendMsg.setEnabled(false);
					this.textAreaHistory.setText("");
					this.textAreaSendMsg.setText("");
					if (o.contains("false")) {
						JOptionPane.showMessageDialog(this, "The other person has closed the chat.", "Chat close", JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					int ans = JOptionPane.showConfirmDialog(this, (String)o, "Chat request", JOptionPane.YES_NO_OPTION);
					if (ans == JOptionPane.YES_OPTION) {
						String s = "Chat request received from ";
						String with = o.substring(s.length());
						controller.acceptChatRequest(with);
						this.setTitle("Chat session between '" + this.controller.getConnectedUser() + "' & '" + with + "'");
						btnSendMsg.setEnabled(true);
					} else {
						controller.refuseChatRequest("refused");
					}
				}
			}
		}
	}
}