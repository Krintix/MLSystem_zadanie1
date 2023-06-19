package mlsystem.shared;

import java.io.IOException;
import java.net.SocketException;
import org.apache.logging.log4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpInterface {
    private Logger logger;
    private Snmp snmp;
    private USM usm;

    public SnmpInterface(UdpAddress hostAddress, OctetString localEngineCreateId,UsmUser[] users,Logger logger) throws SocketException
    {
        this.logger = logger;

        SecurityProtocols.getInstance().addDefaultProtocols();
        SecurityProtocols.getInstance().addPrivacyProtocol(new PrivDES());
        SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthSHA());
        SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthMD5());

        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping(hostAddress,true);
        MessageDispatcherImpl messageDispatcherImpl = new MessageDispatcherImpl();
        snmp = new Snmp(messageDispatcherImpl,transport);

        OctetString localEngineId = new OctetString(MPv3.createLocalEngineID(localEngineCreateId));

        usm = new USM(SecurityProtocols.getInstance(), localEngineId, 0);
        usm.setEngineDiscoveryEnabled(true);

        if(users!=null)
        {
            for (UsmUser user : users) {
                //usm.addUser(user.getSecurityName(), localEngineId, user);
                usm.addUser(user.getSecurityName(), user);
            }
        }

        SecurityModels.getInstance().addSecurityModel(usm);
        
        MPv3 mPv3 = new MPv3(usm);
        snmp.getMessageDispatcher().addMessageProcessingModel(mPv3);
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
    }
    public void Listen() throws IOException
    {
        snmp.listen();
    }
    public void Close() throws IOException
    {
        snmp.close();
    }
    public Snmp GetSnmpInstance()
    {
        return snmp;
    }
    public USM GetUSMInstance()
    {
        return usm;
    }
    public void SendV2Request(UdpAddress targetAddress, OctetString community, int retries, long timeout, int type, VariableBinding[] vbs)
    {
        PDU pdu = new PDU();
        pdu.addAll(vbs);
        pdu.setType(type);

        CommunityTarget<UdpAddress> target = new CommunityTarget<UdpAddress>();
        target.setCommunity(community);
        target.setAddress(targetAddress);
        target.setVersion(SnmpConstants.version2c);
        target.setRetries(retries);
        target.setTimeout(timeout);

        ResponseListener listener = CreateResponseListener();

        try {
            snmp.send( pdu, target, null, listener);
        } catch (IOException e) {
            logger.error("SNMP Interface can't send message("+e.getMessage()+")");
            e.printStackTrace();
        }
    }
    public void SendV3Request(UdpAddress targetAddress, OctetString securityName, int securityLevel, int retries, long timeout, int type, VariableBinding[] vbs)
    {
        UserTarget<UdpAddress> target = new UserTarget<UdpAddress>();
        target.setSecurityLevel(securityLevel);
        target.setSecurityName(securityName);

        target.setAddress(targetAddress);
        target.setVersion(SnmpConstants.version3);
        target.setRetries(retries);
        target.setTimeout(timeout);

        ScopedPDU pdu = new ScopedPDU();
        pdu.addAll(vbs);
        pdu.setType(type);

        ResponseListener responseListener = CreateResponseListener();
        
        try {
            snmp.send(pdu, target, null, responseListener);
        } catch (IOException e) {
            logger.error("SNMP Interface can't send message("+e.getMessage()+")");
            e.printStackTrace();
        }
    }
    private ResponseListener CreateResponseListener()
    {
        return new ResponseListener() {

            @Override
            public <A extends Address> void onResponse(ResponseEvent<A> event) {
                ((Snmp)event.getSource()).cancel(event.getRequest(), this);
                PDU response = event.getResponse();
                if(event.getError() != null) {
                    logger.info("SNMP Interface got error response("+event.getError().getMessage()+")");
                }
                else if(response == null) {
                    logger.info("SNMP Interface request timed out");
                } 
                else {
                    String logInfo = String.format("SNMP Interface received response from %s\n>%s", event.getPeerAddress().toString(), response.toString());
                    logger.info(logInfo);
                }
            }
        };
    }
}
