import com.sap.it.api.mapping.*

def String formatProduct(String product, MappingContext context) {

    if (product == null || product.trim().isEmpty()) {
        return ""
    }

    product = product.trim()

    // Check if purely numeric
    if (product.matches("\\d+")) {
        // Pad with leading zeros to 18 digits
        return product.padLeft(18, '0')
    } else {
        // Alphanumeric – return as-is
        return product
    }
}
