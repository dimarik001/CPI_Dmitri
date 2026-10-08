import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser
import groovy.xml.*

def Message processData(Message message) {

    def body = message.getBody(String)

    // Parse XML (namespace agnostic)
    def parser = new XmlParser(false, false)
    def xml = parser.parseText(body)

    // Helper to match node name ignoring namespace prefix
    def nodeByName = { parent, name ->
        parent?.children()?.find {
            it.name().toString().endsWith(name)
        }
    }

    // Helper to validate node existence + not empty
    def isValidNode = { node ->
        node && (
            node.children().size() > 0 ||
            (node.text() != null && node.text().trim().length() > 0)
        )
    }

    // ================= Message1 =================
    def msg1 = nodeByName(xml, 'Message1')
    if (!msg1) throw new Exception("Message1 not found")

    def record = msg1.Record[0]
    if (!record) throw new Exception("Record not found in Message1")

    def trsInv = record.TrsInv[0]
    if (!trsInv) throw new Exception("TrsInv not found in Message1")

    // ================= Mandatory OData Validations =================

    def inventoryMovement = nodeByName(trsInv, 'XREF_InventoryMovementType')
    if (!isValidNode(inventoryMovement))
        throw new Exception("Mandatory tag XREF_InventoryMovementType is missing or empty")

    def productType = nodeByName(trsInv, 'A_ProductType')
    if (!isValidNode(productType))
        throw new Exception("Mandatory tag A_ProductType is missing or empty")

    // OPTIONAL: Validate nested UOM only if required in your model
    def uomType = nodeByName(productType, 'A_ProductUnitsOfMeasureType')
    if (uomType && !isValidNode(uomType))
        throw new Exception("A_ProductUnitsOfMeasureType exists but is empty")

    // ================= Message2 =================
    def msg2 = nodeByName(xml, 'Message2')
    if (!msg2) throw new Exception("Message2 not found")

    def storageWrapper = nodeByName(msg2, 'XREF_StorageLocationMvt')
    if (!storageWrapper)
        throw new Exception("XREF_StorageLocationMvt not found in Message2")

    // Get ALL XREF_StorageLocationMvtType nodes
    def storageLocations = storageWrapper.children().findAll {
        it.name().toString().endsWith('XREF_StorageLocationMvtType')
    }

    if (!storageLocations || storageLocations.size() == 0)
        throw new Exception("No XREF_StorageLocationMvtType found in Message2")

    // ================= Insert after FLGBLCK =================
    def children = trsInv.children()

    def flgIndex = children.findIndexOf {
        it.name().toString().endsWith('FLGBLCK')
    }

    int insertIndex = (flgIndex >= 0) ? flgIndex + 1 : children.size()

    // Clone and insert each storage location
    storageLocations.each { storageLoc ->

        def storageNode = new Node(null, 'XREF_StorageLocationMvtType')

        storageLoc.children().each { child ->
            new Node(
                storageNode,
                child.name().toString().tokenize('}').last(),
                child.text()
            )
        }

        children.add(insertIndex, storageNode)
        insertIndex++
    }

    // ================= Output ONLY Record =================
    def writer = new StringWriter()
    def printer = new XmlNodePrinter(new PrintWriter(writer))
    printer.setPreserveWhitespace(true)
    printer.print(record)

    message.setBody(writer.toString())
    return message
}