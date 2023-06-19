package mlsystem.shared.action;

import org.snmp4j.ScopedPDU;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

import mlsystem.shared.SnmpInterface;

public class SNMPAction implements IAction {
    private SnmpInterface snmpInterface;
    private UdpAddress targetAddress;
    private UsmUser usmUser;
    private VariableBinding[] vbs;

    public SNMPAction(SnmpInterface snmpInterface, UdpAddress targetAddress, UsmUser usmUser, VariableBinding[] vbs) {
        this.snmpInterface = snmpInterface;
        this.targetAddress = targetAddress;
        this.usmUser = usmUser;
        this.vbs = vbs;
    }

    @Override
    public void Action(String message) {
        snmpInterface.SendV3Request(targetAddress, usmUser.getSecurityName(), SecurityLevel.AUTH_PRIV, 0, 500L, ScopedPDU.GET, vbs);
    }
}
