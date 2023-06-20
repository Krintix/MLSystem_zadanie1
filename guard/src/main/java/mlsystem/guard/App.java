package mlsystem.guard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

import de.re.easymodbus.server.ModbusServer;
import mlsystem.shared.SnmpInterface;
import mlsystem.shared.action.SNMPAction;
import mlsystem.shared.util.PropertiesUtil;
import mlsystem.shared.util.SNMPUtil;

public class App 
{
    private static Logger logger = LogManager.getLogger(App.class);
    private static SnmpInterface snmpInterface;
    private static StatusChecker statusChecker;
    private static ModbusServer modbusServer;
    public static void main( String[] args )
    {
        try {
            Initialize();
        } catch (IOException | ParseException e) {
            logger.fatal("Cannot start("+e.getMessage()+")");
            e.printStackTrace();
            return;
        }

        try {
            snmpInterface.Listen();
        } catch (IOException e) {
            logger.error("Cannot start SNMP Interface("+e.getMessage()+")");
            e.printStackTrace();
        }
        statusChecker.Start();
        try {
            modbusServer.Listen();
        } catch (IOException e) {
            logger.error("Cannot start Modbus Server("+e.getMessage()+")");
            e.printStackTrace();
        }

        while(modbusServer.coils[0])
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("Guard main thread interrupted");
                e.printStackTrace();
            }
        }
    }
    private static void Initialize() throws IOException, ParseException
    {
        String togglerAdress;
        int modbusHostPort;
        String SNMPHostAdress;
        String SNMPTargetAddress;
        UsmUser usmUser;
        VariableBinding[] vbs;
        
        File file = new File("application.properties");
        if(!file.exists())
        {
            togglerAdress = "http://localhost:5000";
            modbusHostPort = 5003;
            SNMPHostAdress = "localhost/5004";
            SNMPTargetAddress = "localhost/161";
            usmUser = new UsmUser(new OctetString("demoUser"), AuthMD5.ID, new OctetString("auth_password"), PrivDES.ID, new OctetString("priv_password"));
            vbs = new VariableBinding[]{new VariableBinding(SnmpConstants.sysUpTime)};
        }
        else
        {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));

            String[] requiredKeys = new String[]{"TogglerAddress","ModbusHostPort","SNMPHostAddress","SNMPTargetAddress"};
            if(!PropertiesUtil.CheckForKeys(properties, requiredKeys))
                throw new IOException("to few start arguments");

            togglerAdress = properties.getProperty("TogglerAddress");
            modbusHostPort = Integer.parseInt(properties.getProperty("ModbusHostPort"));
            SNMPHostAdress = properties.getProperty("SNMPHostAddress");
            SNMPTargetAddress = properties.getProperty("SNMPTargetAddress");
            
            usmUser = SNMPUtil.GetUsmUser(properties, logger);
            vbs = SNMPUtil.GetVariableBindings(properties, logger);
        }

        snmpInterface = new SnmpInterface(new UdpAddress(SNMPHostAdress), new OctetString("0"), new UsmUser[]{usmUser},logger);

        statusChecker = new StatusChecker(togglerAdress, "/status", "/errormessage", Duration.ofMillis(500),logger);
        SNMPAction errorAction = new SNMPAction(snmpInterface, new UdpAddress(SNMPTargetAddress), usmUser, vbs);
        statusChecker.AddListener(errorAction);

        modbusServer = new ModbusServer();
        modbusServer.setPort(modbusHostPort);

        String logInfo = String.format("Guard started with\nToggler adress = %s\nModbus host port = %s\n>>>SNMP<<<\n>host adress = %s\n>USM user = %s\n>Variable Bindings(",
                                        togglerAdress, modbusHostPort, SNMPHostAdress, usmUser.toString());
        for(int i=0;i<vbs.length;i++)
        {
            logInfo += vbs[i].toString()+(i != vbs.length-1 ? ", " : ")");
        }
        logger.info(logInfo);
    }
}
