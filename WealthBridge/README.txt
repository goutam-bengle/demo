To run the application just run the jar, this will read the default configurations for the connections etc.

java -jar wealthbridge.jar

When the configurations are needs to be changed then the configuration file can be passed as an argument to this jar:

java -jar wealthbridge.jar <Path-to-config>/ApplicationConfiguration.cfg


DEFAULT Configurations are:

[DEFAULT]
ConnectionType=initiator
ReconnectInterval=50
FileStorePath=store
FileLogPath=fixlog
StartTime=00:00:00
EndTime=00:00:00
UseDataDictionary=Y
DataDictionary=FIX44.xml
SocketConnectHost=demofix.swissquote.ch
SocketConnectPort=10613
ResetOnLogon=Y
SocketUseSSL=Y
SocketKeyStore=<empty>
SocketKeyStorePassword=<empty>
SenderLocationID=122.160.79.198
SocectLocalHost=122.160.79.198
# standard config elements

[SESSION]
BeginString=FIX.4.4
SenderCompID=845672
TargetCompID=Swissquote
HeartBtInt=30 
Account=845672
FileStorePath=messagestore
FileLogPath=messagelog

JdbcDriver=com.microsoft.sqlserver.jdbc.SQLServerDriver
JdbcURL=jdbc:sqlserver://localhost:1433;databaseName=quickfix;
JdbcUser=sa
JdbcPassword=aloktest
JdbcStoreMessagesTableName=messages
JdbcStoreSessionsTableName=sessions
JdbcLogHeartBeats=N
JdbcLogEventTable=event_log

JdbcSQL=select * from orders where StatusID=1 and ClOrdID is null
updateJdbcSQL=update orders set StatusID=2 , ClOrdID=? where OrderID=?
