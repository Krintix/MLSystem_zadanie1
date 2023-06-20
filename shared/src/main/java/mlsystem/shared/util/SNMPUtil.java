package mlsystem.shared.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

public class SNMPUtil {
    public static UsmUser GetUsmUser(Properties properties, Logger logger) throws IOException
    {
        String[] requiredKeys = new String[]{"USMUserName","USMUserAuthProtocol","USMUserAuthPassword","USMUserPrivProtocol","USMUserPrivPassword"};
        if(!PropertiesUtil.CheckForKeys(properties,requiredKeys))
            throw new IOException("to few start arguments");

        OctetString userName = new OctetString(properties.getProperty("USMUserName"));
        OID authProtocol = null;
        switch(properties.getProperty("USMUserAuthProtocol").toLowerCase())
        {
            case "md5":
                authProtocol = AuthMD5.ID;
                break;
            case "sha":
                authProtocol = AuthSHA.ID;
                break;
            default:
                throw new IOException("Unsupported private protocol");
        }
        OctetString authPassword = new OctetString(properties.getProperty("USMUserAuthPassword"));
        OID privProtocol = null;
        switch(properties.getProperty("USMUserPrivProtocol").toLowerCase())
        {
            case "des":
                privProtocol = PrivDES.ID;
                break;
            case "3des":
                privProtocol = Priv3DES.ID;
                break;
            default:
                throw new IOException("Unsupported private protocol");
        }
        OctetString privPassword = new OctetString(properties.getProperty("USMUserPrivPassword"));

        return new UsmUser(userName,authProtocol,authPassword,privProtocol,privPassword);
    }
    public static VariableBinding[] GetVariableBindings(Properties properties, Logger logger) throws ParseException
    {
        List<VariableBinding> vbs = new ArrayList<VariableBinding>();

        for(int i=0;properties.containsKey("VariableBinding"+i);i++)
        {
            String vb = properties.getProperty("VariableBinding"+i);
            if(vb.contains("~"))
            {
                OID oid = new OID(vb.substring(0,vb.indexOf("~")));
                String variableText = vb.substring(vb.indexOf("~")+1);
                vbs.add(new VariableBinding(oid, new OctetString(variableText)));
            }
            else
            {
                vbs.add(new VariableBinding(new OID(vb)));
            }
        }

        return vbs.toArray(new VariableBinding[0]);
    }
}
