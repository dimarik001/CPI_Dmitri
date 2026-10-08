import groovy.json.JsonOutput
import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def xml = new XmlSlurper().parseText(message.getBody(String))  // Get the XML payload from CPI
    def jsonList = xml.Product.collect { product ->
        product.children().collectEntries { [(it.name()): it.text().trim() ?: ""] }  // Ensure empty values are ""
    }
    
    def jsonOutput = JsonOutput.toJson(jsonList)  // Convert to JSON string
    message.setBody(jsonOutput)  // Set JSON as the output body
    
    return message
}
