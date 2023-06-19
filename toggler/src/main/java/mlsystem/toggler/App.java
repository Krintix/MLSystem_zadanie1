package mlsystem.toggler;

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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.pi4j.provider.exception.ProviderException;

import de.re.easymodbus.server.ModbusServer;
import mlsystem.shared.SnmpInterface;
import mlsystem.shared.action.SNMPAction;
import mlsystem.shared.util.PropertiesUtil;
import mlsystem.shared.util.SNMPUtil;

@SpringBootApplication
public class App {
	private static Logger logger = LogManager.getLogger(App.class);
	private static SnmpInterface snmpInterface;
	private static GPIOToggler gpioToggler;
	private static ModbusServer modbusServer;

	public static void main(String[] args)
    {
		try {
            Initialize();
        } catch (IOException | ParseException e) {
            logger.fatal("Cannot start("+e.getMessage()+")");
            e.printStackTrace();
        }

        try {
            snmpInterface.Listen();
        } catch (IOException e) {
            logger.error("Cannot start SNMP Interface("+e.getMessage()+")");
            e.printStackTrace();
        }
        try {
            gpioToggler.Start();
        } catch (ProviderException e) {
            logger.error("Cannot start GPIO output("+e.getMessage()+")");
            e.printStackTrace();
        }
        try {
            modbusServer.Listen();
        } catch (IOException e) {
            logger.error("Cannot start Modbus Server("+e.getMessage()+")");
            e.printStackTrace();
        }

		int togglerInterval = 0;
        while(modbusServer.coils[0])
        {
			if(modbusServer.holdingRegisters[0] != togglerInterval)
			{
				try {
                    gpioToggler.SetInterval(Duration.ofMillis(modbusServer.holdingRegisters[0]));
                    togglerInterval = modbusServer.holdingRegisters[0];
                } catch (IOException e) {
                    logger.warn("Attempted to set GPIO Toggler Interval to: "+modbusServer.holdingRegisters[0]);
                }
			}

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
				logger.error("Toggler main thread interrupted");
                e.printStackTrace();
            }
        }
	}
	private static void Initialize() throws IOException, ParseException
	{
		int togglerPIN;
        long togglerInterval;
        int restHostport;
        int modbusHostPort;
        String SNMPHostAdress;
        String SNMPTargetAddress;
        UsmUser usmUser;
        VariableBinding[] vbs;

        File file = new File("application.properties");
        if(!file.exists())
        {
            togglerPIN = 13;
            togglerInterval = 500;
            restHostport = 5000;
            modbusHostPort = 5001;
            SNMPHostAdress = "localhost/5002";
            SNMPTargetAddress = "localhost/161";
            usmUser = new UsmUser(new OctetString("demoUser"), AuthMD5.ID, new OctetString("auth_password"), PrivDES.ID, new OctetString("priv_password"));
            vbs = new VariableBinding[]{new VariableBinding(SnmpConstants.sysUpTime)};
        }
        else
        {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));

            String[] requiredKeys = new String[]{"GPIOTogglePIN","GPIOInterval","RESTHostPort","ModbusHostPort","SNMPHostAddress","SNMPTargetAddress"};
            if(!PropertiesUtil.CheckForKeys(properties, requiredKeys))
                throw new IOException("to few start arguments");

            togglerPIN = Integer.parseInt(properties.getProperty("GPIOTogglePIN"));
            togglerInterval = Long.parseLong(properties.getProperty("GPIOInterval"));
            restHostport = Integer.parseInt(properties.getProperty("RESTHostPort"));
            modbusHostPort = Integer.parseInt(properties.getProperty("ModbusHostPort"));
            SNMPHostAdress = properties.getProperty("SNMPHostAddress");
            SNMPTargetAddress = properties.getProperty("SNMPTargetAddress");

            usmUser = SNMPUtil.GetUsmUser(properties, logger);
            vbs = SNMPUtil.GetVariableBindings(properties, logger);
        }

        SpringApplication.run(App.class, "--server.port="+restHostport);

        snmpInterface = new SnmpInterface(new UdpAddress(SNMPHostAdress), new OctetString("0"), new UsmUser[]{usmUser},logger);

        gpioToggler = new GPIOToggler(togglerPIN, Duration.ofMillis(togglerInterval), logger);
        SNMPAction errorAction = new SNMPAction(snmpInterface, new UdpAddress(SNMPTargetAddress), usmUser, vbs);
        gpioToggler.AddListener(errorAction);

        modbusServer = new ModbusServer();
        modbusServer.setPort(modbusHostPort);

        String logInfo = String.format("GPIO Toggler started with\n>>>GPIO<<<\n>Toggle PIN adress = %s\n>Interaval = %s\nSpring(REST) port = %s\nModbus host port = %s\n>>>SNMP<<<\n>host adress = %s\n>USM user = %s\n>Variable Bindings(",
                                        togglerPIN, togglerInterval, restHostport, modbusHostPort, SNMPHostAdress, usmUser.toString());
        for(int i=0;i<vbs.length;i++)
        {
            logInfo += vbs[i].toString()+(i != vbs.length-1 ? ", " : ")");
        }
        logger.info(logInfo);
	}
}
