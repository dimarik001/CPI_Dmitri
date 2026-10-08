import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap

def processData(Message message) {
    def body = message.getBody(String) // assuming XML as string
    def properties = message.getProperties()
    def messageLog = messageLogFactory.getMessageLog(message)    
    
    def customer = properties.get("BusinessPartner")?.replaceFirst('^0+', '')
    if (customer ) {
        messageLog?.addCustomHeaderProperty("Customer", customer )
    }
    
    // Replace only if starts with '00' (first two digits)
    body = body.replaceFirst("<KUNNR>00", "<KUNNR>")
    message.setBody(body)
    return message
}
