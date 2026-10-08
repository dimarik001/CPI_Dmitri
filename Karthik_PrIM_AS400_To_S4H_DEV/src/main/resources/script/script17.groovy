import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser
import groovy.xml.XmlNodePrinter
import groovy.xml.Namespace

def Message processData(Message message) {

    def body = message.getBody(String)

    // Parse XML (namespace-aware, CPI safe)
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

    // ================= Message2 =================
    def storageRoot =
        xml[mm.Message2]?.XREF_StorageLocationMvt

    boolean isStorageLocEmpty =
        !storageRoot || storageRoot.children().size() == 0

    // Set property for Router
    message.setProperty("IsStorageLocEmpty", isStorageLocEmpty)

    // Clone storage node ONLY if not empty
    def storageNode = null
    if (!isStorageLocEmpty) {
        def storageLoc = storageRoot.XREF_StorageLocationMvtType[0]
        storageNode = new Node(null, 'XREF_StorageLocationMvtType')
        storageLoc.children().each { child ->
            new Node(storageNode, child.name(), child.text())
        }
    }

    // ================= Message3 =================
    def productBatch =
        xml[mm.Message3]
            ?.MapProductBatchP
            ?.ProductBatch
            ?.getAt(0)

    def pbNode = null
    if (productBatch) {
        pbNode = new Node(null, 'ProductBatch')
        new Node(pbNode, 'CustomerDate', productBatch.CustomerDate?.text())
        new Node(pbNode, 'SignificantDate', productBatch.SignificantDate?.text())
        new Node(pbNode, 'BatchID', productBatch.BatchID?.text())
        new Node(pbNode, 'Product', productBatch.Product?.text())
        new Node(pbNode, 'Lifespan', productBatch.Lifespan?.text())
    }

    // ================= Insert nodes =================
    def children = trsInv.children()
    def flgIndex = children.findIndexOf { it.name() == 'FLGBLCK' }

    if (flgIndex >= 0) {
        int pos = flgIndex + 1
        if (storageNode) {
            trsInv.children().add(pos++, storageNode)
        }
        if (pbNode) {
            trsInv.children().add(pos, pbNode)
        }
    } else {
        if (storageNode) trsInv.append(storageNode)
        if (pbNode) trsInv.append(pbNode)
    }

    // ================= Output ONLY Record =================
    def writer = new StringWriter()
    def printer = new XmlNodePrinter(new PrintWriter(writer))
    printer.setPreserveWhitespace(true)
    printer.print(record)

    message.setBody(writer.toString())
    return message
}
