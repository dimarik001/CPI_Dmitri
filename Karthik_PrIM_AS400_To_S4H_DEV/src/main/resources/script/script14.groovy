import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonBuilder

def Message processData(Message message) {

    def properties = message.getProperties()

   // Build JSON exactly as per required format
    def json = new JsonBuilder([
        CMPNO : properties.get("CMPNO"),
        DBATCH: properties.get("DBATCH"),
        DSEQ  : properties.get("DSEQ"),
        DDTTR : properties.get("DDTTR"),
        DocS4 : properties.get("DocS4")
    ])

    message.setHeader("Content-Type", "application/json")
    message.setBody(json.toPrettyString())

    return message
}
