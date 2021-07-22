/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sprint.test.plugin;

import com.dataaccess.webservicesserver.NumberConversion;
import com.dataaccess.webservicesserver.NumberConversionSoapType;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.Map;
import pl.sprint.chatbot.client.service.ClientService;
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
    
    private final ClientService clientService; 

    public Main() {
        setLogger("test-plugin");
        Conf.configure("webservice.properties"); //name of config file if used [path=$SPRINBOTSERVER/config/plugins/services.properties]           
        endpoint = Conf.getValue("endpoint", "https://www.dataaccess.com/webservicesserver/NumberConversion.wso?wsdl");
        timeout = Integer.parseInt(Conf.getValue("timeout", "8000"));    
        
        String clientservice = Conf.getValue("clientservice", "https://192.168.254.159:8443/api");
        
        clientService = new ClientService(clientservice);
        
        log("endpoint = " + endpoint + ", timeout = " + timeout, "");
    }
    
    public static void main(String[] args) {
        
        Main m = new Main(); 
        
        
        String ret = m.processCustomResultPocessor("54c2f6f0-e4ae-45b2-8ef3-4c14a240bbf1", "", "getdata");
        
        System.out.println("ret = " + ret);
        
    }

    @Override
    public String processCustomResultPocessor(String session, String parameter, String method) {
        log("parameter: " + parameter + " method: " + method, session);
        
        Utils.trustAllCertyficates();
        
        if(method.equals("changenumtostring"))
        {
            try { 
                NumberConversion numberConversion = new NumberConversion(Utils.createEndpointUrl(endpoint, timeout));
                
                NumberConversionSoapType port = numberConversion.getNumberConversionSoap();
                
                long param = Long.valueOf(parameter);
                
                BigInteger bi = BigInteger.valueOf(param); 
                
                String ret = port.numberToWords(bi);
                
                log(ret, session);
                
                return ret;
                
                
            } catch (MalformedURLException ex) {
                log("autoGenerationRZK error: " + ex, session);
                logException(ex, session);   
            }
        }
        else if(method.equals("getdata"))
        {
            try { 
                
                
                
                Map<String,String> map = clientService.getData(session);
                
                
                
                String ani = map.getOrDefault("ani", ""); 
                String dnis = map.getOrDefault("dnis", ""); 
                String vdn = map.getOrDefault("vdn", ""); 
                
                log(ani,session);
                
                
                return ani + "###" + dnis + "###" + vdn; 
                
            } catch (Exception ex) {
                log("autoGenerationRZK error: " + ex, session);
                logException(ex, session);   
            }
        }
        
        return "ERR";
    }

    @Override
    public void setLogger(String logname) {
         Logger.getInstance().setLogger(logname);
    }

    @Override
    public void log(String message, String session) {
        Logger.getInstance().WriteToLog("Main " + session + " : " + message);
        System.out.println(message);
    }
    
    private void logException(Exception ex, String session)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);                    
        log("getProcesses error: " + sw.toString(), session); 
    }
    
}
