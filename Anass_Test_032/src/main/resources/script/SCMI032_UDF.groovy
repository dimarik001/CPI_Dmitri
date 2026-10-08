
import com.sap.it.api.mapping.*









//------ENTRY_UOM------------

import com.sap.aii.mapping.api.*

def void determineBaseUom(
        String[] matnr,
        String[] charcValue,   // MUST be MATERIAL_TYPE only
        String[] matkl,
        String[] xrefBuom,

        // NEW inputs for ZRAW rules
        String[] maraMeins,    // E1MARAM-MEINS from S/4
        String[] marmMeinh,    // E1MARMM-MEINH from S/4
        String[] ty2tq,        // E1MARMM/_-CWM_-TY2TQ from S/4

        Output output,
        MappingContext context) {

    // ----------------------------
    // Safe reads
    // ----------------------------
    String v_matnr = ""
    if (matnr != null && matnr.length > 0 && matnr[0] != null) {
        v_matnr = matnr[0].trim()
    }

    String v_type = ""
    if (charcValue != null && charcValue.length > 0 && charcValue[0] != null) {
        v_type = charcValue[0].trim().toUpperCase()
    }

    String v_matkl = ""
    if (matkl != null && matkl.length > 0 && matkl[0] != null) {
        v_matkl = matkl[0].trim().toUpperCase()
    }

    String v_maraMeins = ""
    if (maraMeins != null && maraMeins.length > 0 && maraMeins[0] != null) {
        v_maraMeins = maraMeins[0].trim().toUpperCase()
    }

    // ----------------------------
    // Helper: clean value
    // ----------------------------
    def cleanValue = { String s ->
        if (s == null) {
            return ""
        }

        String t = s.trim().toUpperCase()

        if (t.length() == 0) {
            return ""
        }

        if ("/".equals(t) || "NULL".equals(t) || "<NULL>".equals(t)) {
            return ""
        }

        return t
    }

    // ----------------------------
    // Helper: convert UOM to ISO
    // ----------------------------
    def toIsoUom = { String s ->
        String u = cleanValue(s)

        if ("KG".equals(u)) {
            return "KGM"
        }

        if ("CAR".equals(u)) {
            return "CT"
        }

        if ("PAL".equals(u)) {
            return "PF"
        }

        if ("BAG".equals(u)) {
            return "BG"
        }

        return u
    }

    v_maraMeins = toIsoUom(v_maraMeins)

    // ========================================================
    // STEP 1 — XREF wins
    // Lookup material in XREF ZCA0_XPINTYSGOLD.
    // If found, send BUOM column 4.
    // Always send ISO code.
    // ========================================================
    String v_xrefBuom = ""

    if (xrefBuom != null) {
        for (int i = 0; i < xrefBuom.length; i++) {
            String b = cleanValue(xrefBuom[i])

            if (b.length() > 0) {
                v_xrefBuom = toIsoUom(b)
                break
            }
        }
    }

    if (v_matnr.length() > 0 && v_xrefBuom.length() > 0) {
        output.addValue(v_xrefBuom)
        return
    }

    // ========================================================
    // Helper — check if MARM-MEINH = BAG/BG exists
    // ========================================================
    boolean hasBag = false

    if (marmMeinh != null) {
        for (int i = 0; i < marmMeinh.length; i++) {
            String u = toIsoUom(marmMeinh[i])

            if ("BG".equals(u)) {
                hasBag = true
                break
            }
        }
    }

    // ========================================================
    // Helper — check if MARM/_-CWM_-TY2TQ = A exists
    // ========================================================
    boolean hasTy2tqA = false

    if (ty2tq != null) {
        for (int i = 0; i < ty2tq.length; i++) {
            String t = cleanValue(ty2tq[i])

            if ("A".equals(t)) {
                hasTy2tqA = true
                break
            }
        }
    }

    // ========================================================
    // STEP 2 — MATERIAL_TYPE logic
    // ========================================================

    // Scenario 1:
    // MTART CharValue = ZPRI -> KGM
    if ("ZPRI".equals(v_type)) {
        output.addValue("KGM")
        return
    }

    // Scenario 2:
    // MTART CharValue = ZFGD -> CT
    if ("ZFGD".equals(v_type)) {
        output.addValue("CT")
        return
    }

    // Scenario 3:
    // MTART CharValue = ZSFG -> KGM
    if ("ZSFG".equals(v_type)) {
        output.addValue("KGM")
        return
    }

    // Scenario 4:
    // MTART CharValue = ZRAW
    if ("ZRAW".equals(v_type)) {

        // 4.1 If MARM-MEINH = BAG/BG exists in S/4 -> KGM
        if (hasBag) {
            output.addValue("KGM")
            return
        }

        // 4.2 If MARA-MATKL = Z2 and MARA-MEINS = CS in S/4 -> CT
        if ("Z2".equals(v_matkl) && "CS".equals(v_maraMeins)) {
            output.addValue("CT")
            return
        }

        // 4.3 If MARA-MATKL = Z2 and TY2TQ = A in S/4 -> KGM
        if ("Z2".equals(v_matkl) && hasTy2tqA) {
            output.addValue("KGM")
            return
        }

        // 4.4 All other ZRAW -> KGM
        output.addValue("KGM")
        return
    }

    // Safe default
    output.addValue("KGM")
}












//------ENTRY_QNT------------

import java.math.BigDecimal
import java.math.RoundingMode

def void determineEntryQnt(
        String[] entryQnt,     // E1BP2017_GM_ITEM_CREATE-ENTRY_QNT
        String[] entryUom,     // E1BP2017_GM_ITEM_CREATE-ENTRY_UOM
        String[] xrefBuom,     // XREF ZCA0_XPINTYSGOLD-BUOM (could be empty)
        String[] marmUmrez,    // MARM-UMREZ (numerator)
        String[] marmUmren,    // MARM-UMREN (denominator)
        Output output,
        MappingContext context) {

    // ---- helpers (take first non-empty) ----
    def firstNonEmpty = { String[] a ->
        if (a == null) return ""
        for (int i = 0; i < a.length; i++) {
            if (a[i] != null && a[i].trim().length() > 0) return a[i].trim()
        }
        return ""
    }

    String v_entryQnt  = firstNonEmpty(entryQnt)
    String v_entryUom  = firstNonEmpty(entryUom).toUpperCase()
    String v_xrefBuom  = firstNonEmpty(xrefBuom).toUpperCase()
    String v_umrezStr  = firstNonEmpty(marmUmrez)
    String v_umrenStr  = firstNonEmpty(marmUmren)

    // If no quantity, return empty (or you can return 0)
    if (v_entryQnt.length() == 0) {
        output.addValue("")
        return
    }

    // STEP 1: If ENTRY_UOM == XREF BUOM => passthrough
    if (v_xrefBuom.length() > 0 && v_entryUom.length() > 0 && v_entryUom == v_xrefBuom) {
        output.addValue(v_entryQnt)
        return
    }

    // STEP 2: Convert: ENTRY_QNT * UMREZ / UMREN
    try {
        BigDecimal qnt   = new BigDecimal(v_entryQnt)
        BigDecimal umrez = (v_umrezStr.length() > 0) ? new BigDecimal(v_umrezStr) : null
        BigDecimal umren = (v_umrenStr.length() > 0) ? new BigDecimal(v_umrenStr) : null

        // If conversion factors missing/invalid -> passthrough as safest behavior
        if (umrez == null || umren == null || umren.compareTo(BigDecimal.ZERO) == 0) {
            output.addValue(v_entryQnt)
            return
        }

        BigDecimal result = qnt.multiply(umrez).divide(umren, 6, RoundingMode.HALF_UP)

        // Emit without scientific notation
        output.addValue(result.stripTrailingZeros().toPlainString())
        return

    } catch (Exception e) {
        // Any parsing issue -> passthrough
        output.addValue(v_entryQnt)
        return
    }
}

//----------Format material number---------------

import com.sap.it.api.mapping.*

def String formatMaterial(String matnr) {

    if(matnr == null || matnr.trim().isEmpty()){
        return matnr
    }

    matnr = matnr.trim()

    // Check if the value is numeric only
    if(matnr ==~ /^\d+$/){
        // Pad with leading zeros up to 18 characters
        return matnr.padLeft(18,'0')
    }

    // If alphanumeric return as is
    return matnr
}


//---------------------------------------------------------------------

import com.sap.it.api.mapping.*

def void determineEntryUom(
    String[] entryUomP,
    String[] cwmNode,
    String[] fallbackUom,
    Output output
) {
    boolean cwmExists = cwmNode != null && cwmNode.length > 0

    String[] selected = cwmExists ? entryUomP : fallbackUom

    if (selected == null) {
        return
    }

    selected.each { value ->
        if (value != null && value.trim().length() > 0) {
            output.addValue(value.trim())
        }
    }
}