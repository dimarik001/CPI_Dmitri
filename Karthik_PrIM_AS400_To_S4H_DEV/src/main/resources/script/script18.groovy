import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser
import groovy.xml.XmlNodePrinter
import groovy.xml.Namespace

def Message processData(Message message) {

    def body = message.getBody(String)

    // Parse XML (namespace-aware)
    def parser = new XmlParser(false, true)
    def xml = parser.parseText(body)

    def mm = new Namespace(
        "http://sap.com/xi/XI/SplitAndMerge", "multimap"
    )

    // ================= Message1 =================
    def record = xml[mm.Message1]?.Record?.getAt(0)
    if (!record) {
        throw new Exception("Record not found in Message1")
    }

    def trsInv = record.TrsInv[0]

    // ================= Message2 : ProductBatch =================
    def productBatch =
        xml[mm.Message2]
            ?.MapProductBatchP
            ?.ProductBatch
            ?.getAt(0)

    if (!productBatch) {
        // Safety exit – router should already prevent this
        return message
    }

    // Clone ProductBatch
    def pbNode = new Node(null, 'ProductBatch')
    new Node(pbNode, 'CustomerDate', productBatch.CustomerDate?.text())
    new Node(pbNode, 'SignificantDate', productBatch.SignificantDate?.text())
    new Node(pbNode, 'BatchID', productBatch.BatchID?.text())
    new Node(pbNode, 'Product', productBatch.Product?.text())
    new Node(pbNode, 'Lifespan', productBatch.Lifespan?.text())

    // ================= Insert after FLGBLCK =================
    def children = trsInv.children()
    def flgIndex = children.findIndexOf { it.name() == 'FLGBLCK' }

    if (flgIndex >= 0) {
        trsInv.children().add(flgIndex + 1, pbNode)
    } else {
        trsInv.append(pbNode)
    }

    // ================= Output ONLY Record =================
    def writer = new StringWriter()
    def printer = new XmlNodePrinter(new PrintWriter(writer))
    printer.setPreserveWhitespace(true)
    printer.print(record)

    message.setBody(writer.toString())
    return message
}
