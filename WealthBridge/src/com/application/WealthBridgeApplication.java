package com.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.order.Order;
import com.order.OrderSide;
import com.order.OrderTIF;
import com.order.OrderType;

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
		System.out.println("Hi"+arg0);

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

	
	private void sendOrderData(SessionID sessionId) {
		 String sql = "SELECT * FROM Orders";
		System.out.println("read data");
		 // Create a variable for the connection string.
			String connectionUrl = "jdbc:sqlserver://localhost:1433;" +
			        "databaseName=quickfix;";
			String username = "sa";
			String password = "aloktest";

			// Declare the JDBC objects.
			Connection conn = null;
			try {
			    // Establish the connection.
			    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			    conn = DriverManager.getConnection(connectionUrl, username, password);
			    Statement statement = conn.createStatement();
			    ResultSet result = statement.executeQuery(sql);

			    int count = 0;

			    while (result.next()){
			            String OrdType = result.getString("OrdType");
			            String Side = result.getString("Side");
			            String Symbol = result.getString("Symbol");
			            String Account = result.getString("Account");
			            String ISIN = result.getString("ISIN");
			            String SecurityIDSource = result.getString("SecurityIDSource");
			            char HandlInst = result.getString("HandlInst").charAt(0);
			            String ExDestination = result.getString("ExDestination");
			            String Currency = result.getString("Currency");
			            double Price = result.getDouble("Price");
			            int Quantity = result.getInt("Quantity");

			            Order order = new Order();
			                if ("2".equals(OrdType))
			                    order.setType(OrderType.LIMIT);
			                if ("1".equals(Side))
			                    order.setSide(OrderSide.BUY);
			                else
			                    order.setSide(OrderSide.SELL);
			                order.setSymbol(Symbol);
			                order.setAccount(Account);
			                order.setISIN(ISIN);
			                order.setSecurityIDSource(SecurityIDSource);
			                order.setHandlInst(HandlInst);
			                order.setExDestination(ExDestination);
			                order.setCurrency(Currency);
			                order.setLimit(Price);
			                order.setTIF(OrderTIF.DAY);
			                order.setQuantity(Quantity);
			                order.setSessionID(sessionId);
			                application.send(order);   
			                try{Thread.sleep(30000);}catch(InterruptedException exc){System.out.println(exc);}
			    }
			}
			catch (SQLException ex) {
			ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
			}
		}

}
