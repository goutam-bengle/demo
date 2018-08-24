package com.application;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;

public class WealthBridgeApplication implements Application {
	private ObservableLogon observableLogon = new ObservableLogon();

	@Override
	public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		// TODO Auto-generated method stub

	}

	@Override
	public void fromApp(Message arg0, SessionID arg1) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCreate(SessionID arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLogon(SessionID arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLogout(SessionID arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void toAdmin(Message arg0, SessionID arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
		// TODO Auto-generated method stub

	}

	private void send(quickfix.Message message, SessionID sessionID) {
		try {
			Session.sendToTarget(message, sessionID);
		} catch (SessionNotFound e) {
			System.out.println(e);
		}
	}

	public void addLogonObserver(Observer observer) {
		observableLogon.addObserver(observer);
	}

	public void deleteLogonObserver(Observer observer) {
		observableLogon.deleteObserver(observer);
	}

	private static class ObservableLogon extends Observable {
		private HashSet<SessionID> set = new HashSet<SessionID>();

		public void logon(SessionID sessionID) {
			set.add(sessionID);
			setChanged();
			notifyObservers(new LogonEvent(sessionID, true));
			clearChanged();
		}

		public void logoff(SessionID sessionID) {
			set.remove(sessionID);
			setChanged();
			notifyObservers(new LogonEvent(sessionID, false));
			clearChanged();
		}
	}
}
