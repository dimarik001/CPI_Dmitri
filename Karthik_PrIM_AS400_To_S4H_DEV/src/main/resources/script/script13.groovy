import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.*

def Message processData(Message message) {

    def body = message.getBody(String)

    // Parse XML
    def parser = new XmlParser(false, false)
    def xml = parser.parseText(body)

    // Read CMPNO
    def cmpnoNode = xml.CMPNO[0]
    if (!cmpnoNode) {
        throw new Exception("CMPNO not found in payload")
    }

    def cmpnoValue = cmpnoNode.text()

    // Add leading zero if not present
    if (!cmpnoValue.startsWith("0")) {
        cmpnoValue = "0${cmpnoValue}"
        cmpnoNode.value = cmpnoValue
    }

    // 🔹 Set CMPNO as Message Property
    message.setProperty("CMPNO", cmpnoValue)

    // Convert back to XML
    def writer = new StringWriter()
    def printer = new XmlNodePrinter(new PrintWriter(writer))
    printer.setPreserveWhitespace(true)
    printer.print(xml)

    message.setBody(writer.toString())
    return message
}
