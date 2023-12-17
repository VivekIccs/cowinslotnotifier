package com.corona.cowinslotnotifier.Notifier;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

public class Notifier {
	public static void displayNotificationTray(String message) {
		try {
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
			TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
			trayIcon.setImageAutoSize(true);
			trayIcon.setToolTip("System tray icon demo");
			tray.add(trayIcon);
			trayIcon.displayMessage("Vaccine Available", message, MessageType.INFO);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
}
