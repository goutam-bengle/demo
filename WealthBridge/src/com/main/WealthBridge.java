package com.main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import com.application.WealthBridgeApplication;
import com.connector.QuickFixConnector;

import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.JdbcLogFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

/****
 * 
 * @author Sterlite Class will run the application and get all the data from the
 *         datasource and send it quikfix server.
 */
public class WealthBridge {
	
	

	/***
	 * Will the application. Starting point.
	 * 
	 * @param args
	 */

	private boolean initiatorStarted = false;
	private Initiator initiator = null;
	private static WealthBridge wealthBridge = null;
	private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
	private static QuickFixConnector quickFixConnector = null;
	
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public WealthBridge(String[] args) throws Exception {
		InputStream inputStream = null;
		if (args.length == 0) {
			inputStream = new BufferedInputStream(new FileInputStream(new File("config/ApplicationConfiguration.cfg")));
		} else if (args.length == 1) {
			inputStream = new FileInputStream(args[0]);
		}
		if (inputStream == null) {
			System.out.println("usage: " + WealthBridge.class.getName() + " [configFile].");
			return;
		}
		SessionSettings settings = new SessionSettings(inputStream);
		inputStream.close();
		boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "true")).booleanValue();

		WealthBridgeApplication application = new WealthBridgeApplication();
		MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
		LogFactory logFactory = new JdbcLogFactory(settings);
		MessageFactory messageFactory = new DefaultMessageFactory();

		initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory);
		quickFixConnector = new QuickFixConnector(application , settings);
	}

	/***
	 * 
	 */
	public synchronized void logon() {
		if (!initiatorStarted) {
			try {
				initiator.start();
				initiatorStarted = true;
			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
			while (sessionIds.hasNext()) {
				SessionID sessionId = (SessionID) sessionIds.next();
				Session.lookupSession(sessionId).logon();
			}
		}
	}

	/***
	 * 
	 */
	public void logout() {
		Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
		while (sessionIds.hasNext()) {
			SessionID sessionId = (SessionID) sessionIds.next();
			Session.lookupSession(sessionId).logout("user requested");
		}
	}

	public void stop() {
		shutdownLatch.countDown();
	}


	public static void main(String[] args) throws Exception {
		wealthBridge = new WealthBridge(args);
		if (!System.getProperties().containsKey("openfix")) {
			wealthBridge.logon();
		}
		shutdownLatch.await();
	}

}
