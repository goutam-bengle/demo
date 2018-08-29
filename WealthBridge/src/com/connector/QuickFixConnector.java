package com.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.application.LogonEvent;
import com.application.WealthBridgeApplication;
import com.order.Order;
import com.order.OrderSide;
import com.order.OrderTIF;
import com.order.OrderType;

import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;

/****
 * 
 * @author Sterlite
 *Class will act as bridge between Database and QuickFixSever
 *It serves the the data from Datasource once Server is connected.
 */
public class QuickFixConnector implements Observer {

	private SessionSettings settings;
	
	WealthBridgeApplication application;
	private final Logger log = LoggerFactory.getLogger(getClass());	

	public QuickFixConnector(WealthBridgeApplication app, SessionSettings settings) {
		this.application = app;
		this.settings = settings;
		app.addLogonObserver(this);	
	}

	@Override
    public void update(Observable o, Object arg) {
        LogonEvent logonEvent = (LogonEvent) arg;
        if (logonEvent.isLoggedOn()) {
            try {
                sendOrderData(logonEvent.getSessionID());
            } catch (ConfigError e) {
                e.printStackTrace();
            } catch (FieldConvertError e) {
                e.printStackTrace();
            }
            log.debug("Logon SuccessFull with sessionId : " + logonEvent.getSessionID());
            System.out.println("logon");
        } else {
            log.debug("Logon failed with sessionId : " + logonEvent.getSessionID());
            System.out.println("not loggedIn "+logonEvent.getSessionID());
        }
    }

	/**
	 * Pull the orderData from Database and send to QuickFix
	 * 
	 * @param sessionId
	 * @throws ConfigError
	 * @throws FieldConvertError
	 */
    private void sendOrderData(SessionID sessionId) throws ConfigError, FieldConvertError {
        String sql = settings.getString(sessionId, "JdbcSQL");
        System.out.println("read data sql " + sql);
        // Create a variable for the connection string.
        String connectionUrl = settings.getString(sessionId, "JdbcURL");

        String username = settings.getString(sessionId, "JdbcUser");
        String password = settings.getString(sessionId, "JdbcPassword");

        // Declare the JDBC objects.
        Connection conn = null;
        Statement statement = null;
        try {
            // Establish the connection.
            Class.forName(settings.getString(sessionId, "JdbcDriver"));
            conn = DriverManager.getConnection(connectionUrl, username, password);
            statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);

            int count = 0;

            while (result.next()) {
                String ordType = result.getString("OrdType");
                String side = result.getString("Side");
                String symbol = result.getString("Symbol");
                String account = result.getString("Account");
                String isin = result.getString("ISIN");
                String securityIDSource = result.getString("SecurityIDSource");
                char handlInst = result.getString("HandlInst").charAt(0);
                String exDestination = result.getString("ExDestination");
                String currency = result.getString("Currency");
                double price = result.getDouble("Price");
                int quantity = result.getInt("Quantity");

                Order order = new Order();
                if ("2".equals(ordType))
                    order.setType(OrderType.LIMIT);
                if ("1".equals(side))
                    order.setSide(OrderSide.BUY);
                else
                    order.setSide(OrderSide.SELL);
                order.setSymbol(symbol);
                order.setAccount(account);
                order.setISIN(isin);
                order.setSecurityIDSource(securityIDSource);
                order.setHandlInst(handlInst);
                order.setExDestination(exDestination);
                order.setCurrency(currency);
                order.setLimit(price);
                order.setTIF(OrderTIF.DAY);
                order.setQuantity(quantity);
                order.setSessionID(sessionId);
                application.send(order);
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException exc) {
                    System.out.println(exc);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
        } finally{
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(statement != null){
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            Session.lookupSession(sessionId).logout("user requested");
        }
    }
	   
}
