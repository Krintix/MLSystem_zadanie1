package mlsystem.shared.action;

import java.util.HashMap;
import java.util.Map;

import org.snmp4j.ScopedPDU;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

import mlsystem.shared.SnmpInterface;

public class SNMPAction implements IAction {
    private SnmpInterface snmpInterface;
    private UdpAddress targetAddress;
    private UsmUser usmUser;
    private VariableBinding[] vbs;
    private Map<Integer, String> vbsTemplates = new HashMap<Integer, String>();

    public SNMPAction(SnmpInterface snmpInterface, UdpAddress targetAddress, UsmUser usmUser, VariableBinding[] vbs) {
        this.snmpInterface = snmpInterface;
        this.targetAddress = targetAddress;
        this.usmUser = usmUser;
        this.vbs = vbs;
        for(int i=0;i<vbs.length;i++){
            String variableText = vbs[i].getVariable().toString();
            if(variableText.contains("~"))
                vbsTemplates.put(i, variableText);
        }
    }

    @Override
    public void Action(String message) {
        VariableBinding[] messagevbs = new VariableBinding[vbs.length];
        for(int i=0;i<vbs.length;i++){
            if(vbsTemplates.containsKey(i)){
                String variableText = vbsTemplates.get(i).replace("~", message);
                messagevbs[i] = new VariableBinding(vbs[i].getOid(), new OctetString(variableText));
            } else {
                messagevbs[i] = vbs[i];
            }
        }
        snmpInterface.SendV3Request(targetAddress, usmUser.getSecurityName(), SecurityLevel.AUTH_PRIV, 0, 500L, ScopedPDU.GET, vbs);
    }
}
