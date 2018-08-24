package com.connector;

import java.util.Observable;
import java.util.Observer;

import com.application.WealthBridgeApplication;

import quickfix.SessionID;

/****
 * 
 * @author Sterlite
 *Class will act as bridge between Database and QuickFixSever
 *It serves the the data from Datasource once Server is connected.
 */
public class QuickFixConnector implements Observer {

	private SessionID sessionID;
	private boolean loggedOn;
	
	WealthBridgeApplication application;
	
	

	public SessionID getSessionID() {
		return sessionID;
	}

	public boolean isLoggedOn() {
		return loggedOn;
	}

	public QuickFixConnector(WealthBridgeApplication app) {
		this.application = app;
		app.addLogonObserver(this);	
	}

	@Override
	public void update(Observable o, Object arg) {
		if (isLoggedOn()) {
			System.out.println("logon");
		} else {
			System.out.println("no");
		}

	}

}
