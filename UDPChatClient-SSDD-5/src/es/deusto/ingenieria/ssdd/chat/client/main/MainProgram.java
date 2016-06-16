package es.deusto.ingenieria.ssdd.chat.client.main;

import java.awt.EventQueue;

import es.deusto.ingenieria.ssdd.chat.client.controller.ChatClientController;
import es.deusto.ingenieria.ssdd.chat.client.gui.JFrameMainWindow;

public class MainProgram {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {					
					JFrameMainWindow frame = new JFrameMainWindow(new ChatClientController());
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}