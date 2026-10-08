import com.sap.it.api.mapping.*
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.lang.String

def String mapRegionCode(String land1, MappingContext context) {
    if (land1 == null) return '106'

    switch (land1.toUpperCase()) {
        case 'CA':
            return '100'
        case 'US':
            return '101'
        case 'CN':
            return '102'
        case 'JP':
            return '103'
        case 'KR':
            return '104'
        case 'MX':
            return '105'
        default:
            return '106'
    }
}






//---------------------------------------------------------------------------



import com.sap.it.api.mapping.*

def void determineMAGAS(
        String[] bpIdentificationType,
        String[] bpIdentificationNumber,
        Output output,
        MappingContext context) {

    if (bpIdentificationType == null || bpIdentificationNumber == null) {
        return
    }

    int n = Math.min(bpIdentificationType.length, bpIdentificationNumber.length)

    for (int i = 0; i < n; i++) {
        String type = bpIdentificationType[i]
        String num  = bpIdentificationNumber[i]

        if (type == null) continue
        type = type.trim()

        if (!type.equalsIgnoreCase("ZMGZ")) continue

        if (num != null) {
            num = num.trim()
            if (num.length() > 0 && !num.equalsIgnoreCase("<null>")) {
                output.addValue(num)
            }
        }

        // ZMGZ trouvé → on sort UNE seule valeur
        return
    }

    // Si ZMGZ n'existe pas → rien envoyé (blank)
}




//---------------------------------------------------------------------------




def void determineBpIdentificationNumber(
        String[] bpIdentificationType,
        String[] bpIdentificationNumber,
        Output output,
        MappingContext context) {

    String type0 = (bpIdentificationType != null && bpIdentificationType.length > 0)
            ? clean(bpIdentificationType[0]) : null
    String num0  = (bpIdentificationNumber != null && bpIdentificationNumber.length > 0)
            ? clean(bpIdentificationNumber[0]) : null

    // If first type doesn't exist -> GENE999
    if (type0 == null) {
        output.addValue("GENE999")
        return
    }

    // If first type is ZVILLE -> send first number (or fallback)
    if (type0.equalsIgnoreCase("ZVILLE")) {
        output.addValue(num0 != null ? num0 : "GENE999")
        return
    }

    // Else -> GENE999
    output.addValue("GENE999")
}

private String clean(String s) {
    if (s == null) return null
    s = s.trim()
    if (s.length() == 0) return null
    if (s.equalsIgnoreCase("<null>")) return null
    return s
}


//---------------------------------------------------------------------------




def void removeSuppress(String[] values, Output output, MappingContext context)

{
    values.each 
    { 
        v ->
        if (!output.isSuppress(v)) 
        {
            output.addValue(v)
            
        }
    }
}



//---------------------------------------------

def String getRelationshipName(String partner,String partner2,String relationship, MappingContext context){
    
    if(partner == partner2 && relationship == 'ZZ0002'){
        return partner
    }
    else if(partner == partner2 && relationship == 'ZZ0003'){
        return partner
    }
     else if(partner == partner2 && relationship == 'ZZ0004'){
        return partner
    }
     else if(partner == partner2 && relationship == 'ZZ0005'){
        return partner
    }
    
    else return ''

}

//---------------------------------------------

def String mapActiveFlag(String deletionFlag) {

    // If flag = 'X' → send "0"
    if (deletionFlag?.trim()?.equalsIgnoreCase("X")) {
        return "0"
    }

    // For anything else (null, empty, or any other value) → send "1"
    return "1"
}


//---------------------------------------------

def String getCNAME(String name1, String partner, String kunnr) {
    if (partner == kunnr) {
        if (name1 != null && name1.length() > 0) {
            return name1.length() >= 25 ? name1.substring(0, 25) : name1
        }
    }
    return ""
}

//---------------------------------------------

def String getNOMSU(String name1, String partner, String kunnr, MappingContext context) {
    // Check if partner equals customer number
    if (partner == kunnr) {
        if (name1 != null && name1.length() > 25) {
            // Return characters after position 25
            return name1.substring(25)
        }
    }
    // Return empty string if conditions not met or nothing remains
    return ""
}

//---------------------------------------------

def void getCADD1(String[] stras, String[] kunnr, String[] partner, Output output, MappingContext context) {
    // Defensive null checks
    String strasValue = (stras != null && stras.length > 0) ? stras[0] : ""
    String kunnrValue = (kunnr != null && kunnr.length > 0) ? kunnr[0] : ""
    String partnerValue = (partner != null && partner.length > 0) ? partner[0] : ""

    // Logic: only process if KUNNR == BusinessPartner
    if (kunnrValue.equals(partnerValue)) {
        if (strasValue.length() > 30) {
            output.addValue(strasValue.substring(0, 30))  // first 30 chars
        } else {
            output.addValue(strasValue)  // less than 30 chars — send full
        }
    } else {
        output.addValue("")  // not matching partner
    }
}

//---------------------------------------------

def void getCADD2(String[] stras, String[] kunnr, String[] partner, Output output, MappingContext context) {
    // Defensive null checks
    String strasValue = (stras != null && stras.length > 0) ? stras[0] : ""
    String kunnrValue = (kunnr != null && kunnr.length > 0) ? kunnr[0] : ""
    String partnerValue = (partner != null && partner.length > 0) ? partner[0] : ""

    // Logic: only process if KUNNR == BusinessPartner
    if (kunnrValue.equals(partnerValue)) {
        if (strasValue.length() > 30) {
            output.addValue(strasValue.substring(30))  // remaining after first 30 chars
        } else {
            output.addValue("")  // nothing left
        }
    } else {
        output.addValue("")  // not matching partner
    }
}

//---------------------------------------------

def String mapCMPNO(String VKORG, MappingContext context){
    if (VKORG == null || VKORG.trim().isEmpty()) {
        return ""   // Blank if empty
    } else if (VKORG.trim() == "S100") {
        return "015" // Send '015'
    } else if (VKORG.trim() == "S102") {
        return ""    // Send blank
    } else if (VKORG.trim() == "S300") {
        return ""    // Send blank
    } else {
        return ""    // Default blank
    }
}


//---------------------------------------------


def String mapCXSUB(String KATR1, MappingContext context){
	 if (KATR1 == null || KATR1.trim().isEmpty()) {
        return ""   // Blank if empty
    } else if (KATR1.trim() == "1") {
        return "X"  // Send 'X' if value is '1'
    } else if (KATR1.trim() == "0") {
        return ""   // Blank if '0'
    } else {
        return ""   // Default blank
    }
}

//---------------------------------------------

def String formatDateToYYYYMMDD(String input) {
    if (input == null || input.trim().isEmpty()) {
        return ""
    }

    try {
        def parsedDate = LocalDate.parse(input.substring(0, 10)) // get yyyy-MM-dd
        return parsedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    } catch (Exception e) {
        return ""
    }
}

//---------------------------------------------

def String formatSAPHRCRT(String input) {
    if (input == null || input.trim().isEmpty()) return ""

    try {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime parsed = LocalDateTime.parse(input, inputFormat)

        // Always return HHmmss
        return parsed.format(DateTimeFormatter.ofPattern("HHmmss"))
    } catch (Exception e) {
        return "InvalidTimestamp"
    }
}

//---------------------------------------------CLASM--- 

def String salesSector(String katr5) {
    if (katr5 == null) return ""
    switch(katr5.toUpperCase()) {
        case "5A": return "DET"
        case "5B": return "EXP"
        case "5C": return "AUT"
        case "5D": return "HRI"
        case "5E": return "USA"
        case "5F": return "INT"
        case "5G": return "DUS"
        case "5H": return "DIS"
        case "5I": return "PAT"
        case "5J": return "ROT"
        case "5K": return "DFT"
        default: return katr5  
    }
}

//------------------------------------------------

//---------------------------------


def String mapCusno(String businessPartner, String businessPartnerType) {

    // -------------------------------------------
    // NEW RULE: Replace PLxxxx → 60xxxx (ALL TYPES)
    // -------------------------------------------
    if (businessPartner?.startsWith("PL")) {
        def afterPL = businessPartner.substring(2)
        businessPartner = "60" + afterPL
    }

    // -------------------------------------------
    // Case 1: When BusinessPartnerType = ZPLT
    // (existing logic kept 100% intact)
    // -------------------------------------------
    if (businessPartnerType?.equalsIgnoreCase("ZPLT")) {
        return businessPartner  // already converted above if needed
    }

    // -------------------------------------------
    // Case 2: For all other types
    // (existing logic - unchanged)
    // -------------------------------------------
    def length = businessPartner.length()

    if (length == 6) {
        return businessPartner
    }

    if (length > 3) {
        def first = businessPartner.substring(0, 1)
        def rest = businessPartner.substring(1)
        def cleaned = rest.replaceFirst("^00", "")
        return first + cleaned
    }

    return businessPartner
}


//---------------------------------

def mapRefuseSubstitution(String KATR1) {
    if (KATR1 == null || KATR1.trim().isEmpty()) {
        return ""   // Blank if empty
    } else if (KATR1.trim() == "1") {
        return "X"  // Send 'X' if value is '1'
    } else if (KATR1.trim() == "0") {
        return ""   // Blank if '0'
    } else {
        return ""   // Default blank
    }
}
