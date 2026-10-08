import com.sap.gateway.ip.core.customdev.util.Message

Message processData(Message message) {

    // Read required properties
    def dtypet = message.getProperty("DTYPET")
    def dentex = message.getProperty("DENTEX")
    def dentaf = message.getProperty("DENTAF")

    if (dtypet == null) {
        throw new Exception("DTYPET property is missing")
    }

    // Normalize value (safety)
    dtypet = dtypet.toString().trim()

    // Determine LegHouse
    def legHouse
    if (dtypet == "ET" || dtypet == "RT") {
        legHouse = dentex
    } else {
        legHouse = dentaf
    }

    if (legHouse == null) {
        throw new Exception("LegHouse could not be determined (DENTEX/DENTAF missing)")
    }

    // Set LegHouse for OData query usage
    message.setProperty("LegHouse", legHouse)

    return message
}
