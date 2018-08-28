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
import quickfix.SessionID;
import quickfix.SessionSettings;

/****
 * 
 * @author Sterlite
 *Class will act as bridge between Database and QuickFixSever
 *It serves the the data from Datasource once Server is connected.
 */
public class QuickFixConnector implements Observer {

	private static SessionSettings settings;
	
	WealthBridgeApplication application;
	private final Logger log = LoggerFactory.getLogger(getClass());	
	



    protected SessionSettings getSettings() {
        return settings;
    }

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
        // jdbc:sqlserver://localhost:1433;databaseName=quickfix;
        String username = settings.getString(sessionId, "JdbcUser");
        String password = settings.getString(sessionId, "JdbcPassword");

        // Declare the JDBC objects.
        Connection conn = null;
        try {
            // Establish the connection.
            Class.forName(settings.getString(sessionId, "JdbcDriver"));
            conn = DriverManager.getConnection(connectionUrl, username, password);
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);

            int count = 0;

            while (result.next()) {
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
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException exc) {
                    System.out.println(exc);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
        }
    }
	   
}
