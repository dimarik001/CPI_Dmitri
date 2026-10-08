import com.sap.it.api.mapping.*

def void filterBPKIND(String[] bpKind, Output output, MappingContext context) {
    String kind = (bpKind != null && bpKind.length > 0) ? bpKind[0] : ""

    // If BPKIND is ZECO, ZICR, or ZCAG -> return false (block further logic)
    if (["ZECO", "ZICR", "ZCAG"].contains(kind)) {
        output.addValue(false)
    } else {
        output.addValue(true)
    }
}
