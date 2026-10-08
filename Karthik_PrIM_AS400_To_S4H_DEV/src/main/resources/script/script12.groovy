import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser
import groovy.xml.*

def Message processData(Message message) {

    def body = message.getBody(String)

    // Parse XML (namespace agnostic)
    def parser = new XmlParser(false, false)
    def xml = parser.parseText(body)

    // ================= Message1 =================
    def msg1 = xml.'multimap:Message1'[0]
    if (!msg1) throw new Exception("Message1 not found")

    def record = msg1.Record[0]
    if (!record) throw new Exception("Record not found in Message1")

    def trsInv = record.TrsInv[0]
    if (!trsInv) throw new Exception("TrsInv not found in Message1")

    // ================= Mandatory Validations =================
    def inventoryMovement = trsInv.'XREF_InventoryMovementType'[0]
    if (!inventoryMovement || inventoryMovement.children().isEmpty()) {
        throw new Exception("Mandatory tag XREF_InventoryMovementType missing/empty")
    }

    def productType = trsInv.'A_ProductType'[0]
    if (!productType || productType.children().isEmpty()) {
        throw new Exception("Mandatory tag A_ProductType missing/empty")
    }

    def uomType = productType.'A_ProductUnitsOfMeasureType'[0]
    if (!uomType || uomType.children().isEmpty()) {
        throw new Exception("Mandatory tag A_ProductUnitsOfMeasureType missing/empty")
    }

    // ================= Message2 =================
    def msg2 = xml.'multimap:Message2'[0]
    if (!msg2) throw new Exception("Message2 not found")

    def storageList = 
        msg2.'XREF_StorageLocationMvt'[0]
            .'XREF_StorageLocationMvtType'

    if (!storageList || storageList.isEmpty()) {
        throw new Exception("Mandatory tag XREF_StorageLocationMvtType missing in Message2")
    }

    // Clone ALL StorageLocation nodes
    def storageNodes = []
    storageList.each { storageLoc ->
        def storageNode = new Node(null, 'XREF_StorageLocationMvtType')
        storageLoc.children().each { child ->
            new Node(storageNode, child.name(), child.text())
        }
        storageNodes.add(storageNode)
    }

    // ================= Message3 =================
    def msg3 = xml.'multimap:Message3'[0]
    if (!msg3) throw new Exception("Message3 not found")

    def productBatch =
        msg3.'MapProductBatchP'[0]
            .'ProductBatch'[0]

    if (!productBatch || productBatch.children().isEmpty()) {
        throw new Exception("Mandatory tag ProductBatch missing/empty")
    }

    // Clone ProductBatch node
    def pbNode = new Node(null, 'ProductBatch')
    productBatch.children().each { child ->
        new Node(pbNode, child.name(), child.text())
    }

    // ================= Insert nodes =================
    def children = trsInv.children()
    def flgIndex = children.findIndexOf { it.name() == 'FLGBLCK' }

    if (flgIndex >= 0) {
        int insertPos = flgIndex + 1

        // Insert all storage nodes
        storageNodes.each { node ->
            trsInv.children().add(insertPos++, node)
        }

        // Insert ProductBatch after storage nodes
        trsInv.children().add(insertPos, pbNode)

    } else {

        storageNodes.each { node ->
            trsInv.append(node)
        }

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