package com.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.StringField;
import quickfix.UnsupportedMessageType;
import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.ExDestination;
import quickfix.field.ExecID;
import quickfix.field.HandlInst;
import quickfix.field.LocateReqd;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;

import com.order.Order;
import com.order.OrderSide;
import com.order.OrderTIF;
import com.order.OrderType;

public class WealthBridgeApplication implements Application {

    String  ordId;
    private SessionSettings settings;
    private ObservableOrder observableOrder = new ObservableOrder();
    private ObservableLogon observableLogon = new ObservableLogon();
    private boolean isAvailable = true;
    private boolean isMissingField;
    
    
    static private TwoWayMap sideMap = new TwoWayMap();
    static private TwoWayMap typeMap = new TwoWayMap();
    static private TwoWayMap tifMap = new TwoWayMap();
    static private HashMap<SessionID, HashSet<ExecID>> execIDs = new HashMap<SessionID, HashSet<ExecID>>();
    
	public WealthBridgeApplication(SessionSettings pSettings) {
	    this.settings = pSettings;
    }

    @Override
	public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		// TODO Auto-generated method stub

	}

	@Override
	public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		//updateOrder(message, sessionID);

	}

	@Override
	public void onCreate(SessionID arg0) {
		// TODO Auto-generated method stub

	}
   
	@Override
	public void onLogon(SessionID arg0) {
	    observableLogon.logon(arg0);
		System.out.println("Session created "+arg0);

	}

	@Override
	public void onLogout(SessionID arg0) {
	    observableLogon.logoff(arg0);

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

	public Side sideToFIXSide(OrderSide side) {
        return (Side) sideMap.getFirst(side);
    }

    public OrderSide FIXSideToSide(Side side) {
        return (OrderSide) sideMap.getSecond(side);
    }

    public OrdType typeToFIXType(OrderType type) {
        return (OrdType) typeMap.getFirst(type);
    }

    public OrderType FIXTypeToType(OrdType type) {
        return (OrderType) typeMap.getSecond(type);
    }

    public TimeInForce tifToFIXTif(OrderTIF tif) {
        return (TimeInForce) tifMap.getFirst(tif);
    }

    public OrderTIF FIXTifToTif(TimeInForce tif) {
        return (OrderTIF) typeMap.getSecond(tif);
    }
    
    public void addLogonObserver(Observer observer) {
        observableLogon.addObserver(observer);
    }

    public void deleteLogonObserver(Observer observer) {
        observableLogon.deleteObserver(observer);
    }

    public void addOrderObserver(Observer observer) {
        observableOrder.addObserver(observer);
    }

    public void deleteOrderObserver(Observer observer) {
        observableOrder.deleteObserver(observer);
    }

    private static class ObservableOrder extends Observable {
        public void update(Order order) {
            setChanged();
            notifyObservers(order);
            clearChanged();
        }
    }
   
    private void updateOrder(String clId, SessionID sessionID, int orderId) {

        // Declare the JDBC objects.
        Connection conn = null;
        PreparedStatement preparedStmt = null;
        
        try {
            String sql = settings.getString(sessionID, "updateJdbcSQL");
            System.out.println("read data update sql " + sql);
            // Create a variable for the connection string.
            String connectionUrl = settings.getString(sessionID, "JdbcURL");

            String username = settings.getString(sessionID, "JdbcUser");
            String password = settings.getString(sessionID, "JdbcPassword");

            // Establish the connection.
            Class.forName(settings.getString(sessionID, "JdbcDriver"));
            conn = DriverManager.getConnection(connectionUrl, username, password);
            preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setString(1, clId);
            preparedStmt.setInt(2, orderId);

            // execute the java preparedstatement
            preparedStmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (ConfigError e) {
            e.printStackTrace();
        } catch (FieldConvertError e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStmt != null) {
                try {
                    preparedStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
	public void send(Order order) {
        String beginString = order.getSessionID().getBeginString();
        if (beginString.equals("FIX.4.0"))
            send40(order);
        else if (beginString.equals("FIX.4.1"))
            send41(order);
        else if (beginString.equals("FIX.4.2"))
            send42(order);
        else if (beginString.equals("FIX.4.3"))
            send43(order);
        else if (beginString.equals("FIX.4.4"))
            send44(order);
        return;
    }

    public void send40(Order order) {
        quickfix.fix40.NewOrderSingle newOrderSingle = new quickfix.fix40.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), new OrderQty(order.getQuantity()),
                typeToFIXType(order.getType()));

        send(populateOrder(order, newOrderSingle), order.getSessionID());
    }

    public void send41(Order order) {
        quickfix.fix41.NewOrderSingle newOrderSingle = new quickfix.fix41.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));

        send(populateOrder(order, newOrderSingle), order.getSessionID());
    }

    public void send42(Order order) {
        quickfix.fix42.NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));

        send(populateOrder(order, newOrderSingle), order.getSessionID());
    }

    public void send43(Order order) {
        quickfix.fix43.NewOrderSingle newOrderSingle = new quickfix.fix43.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), sideToFIXSide(order.getSide()),
                new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));
        send(populateOrder(order, newOrderSingle), order.getSessionID());
    }

    public void send44(Order order) {
        // Get order details
        ordId = order.getID();
        quickfix.fix44.NewOrderSingle newOrderSingle = new quickfix.fix44.NewOrderSingle(
                new ClOrdID(order.getID()), sideToFIXSide(order.getSide()), new TransactTime(),
                typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));
        newOrderSingle.set(new SecurityID(order.getISIN()));
        newOrderSingle.set(new SecurityIDSource(order.getSecurityIDSource()));
        newOrderSingle.set(new HandlInst(order.getHandlInst()));
        newOrderSingle.set(new Account(order.getAccount()));
        newOrderSingle.set(new ExDestination(order.getExDestination()));
        newOrderSingle.setField(new StringField(15, order.getCurrency()));
        send(populateOrder(order, newOrderSingle), order.getSessionID());
        updateOrder(order.getID(), order.getSessionID(), order.getOrderId());
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
    
    public quickfix.Message populateOrder(Order order, quickfix.Message newOrderSingle) {

        OrderType type = order.getType();

        if (type == OrderType.LIMIT)
            newOrderSingle.setField(new Price(order.getLimit().doubleValue()));
        else if (type == OrderType.STOP) {
            newOrderSingle.setField(new StopPx(order.getStop().doubleValue()));
        } else if (type == OrderType.STOP_LIMIT) {
            newOrderSingle.setField(new Price(order.getLimit().doubleValue()));
            newOrderSingle.setField(new StopPx(order.getStop().doubleValue()));
        }

        if (order.getSide() == OrderSide.SHORT_SELL
                || order.getSide() == OrderSide.SHORT_SELL_EXEMPT) {
            newOrderSingle.setField(new LocateReqd(false));
        }

        newOrderSingle.setField(tifToFIXTif(order.getTIF()));
        return newOrderSingle;
    }
    
    static {
        sideMap.put(OrderSide.BUY, new Side(Side.BUY));
        sideMap.put(OrderSide.SELL, new Side(Side.SELL));
        sideMap.put(OrderSide.SHORT_SELL, new Side(Side.SELL_SHORT));
        sideMap.put(OrderSide.SHORT_SELL_EXEMPT, new Side(Side.SELL_SHORT_EXEMPT));
        sideMap.put(OrderSide.CROSS, new Side(Side.CROSS));
        sideMap.put(OrderSide.CROSS_SHORT, new Side(Side.CROSS_SHORT));

        typeMap.put(OrderType.MARKET, new OrdType(OrdType.MARKET));
        typeMap.put(OrderType.LIMIT, new OrdType(OrdType.LIMIT));
        typeMap.put(OrderType.STOP, new OrdType(OrdType.STOP));
        typeMap.put(OrderType.STOP_LIMIT, new OrdType(OrdType.STOP_LIMIT));

        tifMap.put(OrderTIF.DAY, new TimeInForce(TimeInForce.DAY));
        tifMap.put(OrderTIF.IOC, new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL));
        tifMap.put(OrderTIF.OPG, new TimeInForce(TimeInForce.AT_THE_OPENING));
        tifMap.put(OrderTIF.GTC, new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
        tifMap.put(OrderTIF.GTX, new TimeInForce(TimeInForce.GOOD_TILL_CROSSING));

    }
    
    public boolean isMissingField() {
        return isMissingField;
    }
    
    public void setMissingField(boolean isMissingField) {
        this.isMissingField = isMissingField;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
    
    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
