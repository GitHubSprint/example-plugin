package pl.sprint.test.plugin;

import com.dataaccess.webservicesserver.NumberConversion;
import com.dataaccess.webservicesserver.NumberConversionSoapType;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import pl.sprint.chatbot.client.service.SprintBotClient;
import pl.sprint.chatbot.ext.lib.ChatBotCustomResultProcessor;
import pl.sprint.chatbot.ext.lib.Conf;
import pl.sprint.chatbot.ext.lib.Utils;
import pl.sprint.chatbot.ext.lib.logger.Logger;

/**
 *
 * @author Sławomir Kostrzewa
 */
public class Main implements ChatBotCustomResultProcessor
{
    private final String endpoint;
    private final int timeout;
    
    private final SprintBotClient clientService;

    public Main() {
        setLogger("test-plugin");
        Conf.configure("webservice.properties"); //name of config file if used [path=$SPRINBOTSERVER/config/plugins/services.properties]           
        endpoint = Conf.getValue("endpoint", "https://www.dataaccess.com/webservicesserver/NumberConversion.wso?wsdl");
        timeout = Integer.parseInt(Conf.getValue("timeout", "8000"));

        String sprintBotClient = Conf.getValue("clientservice", "https://demo.sprintbot.ai:8443/api");
        
        clientService = new SprintBotClient(sprintBotClient);
        
        log("endpoint = " + endpoint + ", timeout = " + timeout, "");
    }
    
    public static void main(String[] args) {
        
        Main m = new Main();
        String session = "9fd26d0c-1ba0-4468-bfb0-2958746b925f";

        String txtNum = m.processCustomResultPocessor(session, "123", "changenumtostring");
        System.out.println("changenumtostring: " + txtNum);

        String test = m.processCustomResultPocessor(session, "123456789###1234###" + txtNum, "setdata");
        System.out.println("setdata: " + test);
        test = m.processCustomResultPocessor(session, "", "getdata");
        System.out.println("getdata: " + test);


    }

    @Override
    public String processCustomResultPocessor(String session, String parameter, String method) {
        log("parameter: " + parameter + " method: " + method, session);

        Utils.trustAllCertyficates();

        try {
            switch (method) {
                case "changenumtostring":
                    NumberConversion numberConversion = new NumberConversion(Utils.createEndpointUrl(endpoint, timeout));
                    NumberConversionSoapType port = numberConversion
                            .getNumberConversionSoap();

                    String ret = port
                            .numberToWords(new BigInteger(parameter, Character.MAX_RADIX));

                    log(ret, session);
                    return ret;
                case "getdata":
                    Map<String, String> map = clientService.getSessionData(session);
                    log("ani: " + map.get("ani") + " dnis: " + map.get("dnis") + " txtNum: " + map.get("txtNum"), session);
                    return "OK";
                case "setdata":
                    String[] parameters = parameter.split("###");
                    if (parameters.length < 3)
                        return "ERR Invalid parameters";
                    String ani = parameters[0];
                    String dnis = parameters[1];
                    String txtNum = parameters[2];

                    Map<String, String> mapInput = new HashMap<>();
                    mapInput.put("ani", ani);
                    mapInput.put("dnis", dnis);
                    mapInput.put("txtNum", txtNum);
                    clientService.updateData(session, mapInput);
                    return "OK";
            }
        } catch (Exception ex) {
            log("processCustomResultProcessor error: " + ex, session);
            logException(ex, session);
        }

        return "ERR";
    }

    @Override
    public void setLogger(String logName) {
         Logger.getInstance().setLogger(logName);
    }

    @Override
    public void log(String message, String session) {
        Logger.getInstance().WriteToLog(session + ": Main " + message);
    }
    
    private void logException(Exception ex, String session) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);                    
        log("getProcesses error: " + sw, session);
    }
    
}
