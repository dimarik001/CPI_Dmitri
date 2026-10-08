import com.sap.it.api.mapping.*

def void getCatchWeightValueByItemIndex(
        String[] itemDriver,
        String[] matdocItems,
        String[] catchWeightValues,
        Output output,
        MappingContext context) {

    /*
     * itemDriver:
     * One value for every standard item, for example MATERIAL.
     *
     * matdocItems:
     * Catch-weight MATDOC_ITM values, for example:
     * 0002, 0003.
     *
     * catchWeightValues:
     * Catch-weight values corresponding to MATDOC_ITM,
     * for example ENTRY_QNT_PME:
     * 1200.000, 300.000.
     */

    Map<Integer, String> catchWeightByItem = new HashMap<Integer, String>()

    // Build:
    // 2 -> 1200.000
    // 3 -> 300.000
    for (int i = 0; i < matdocItems.length; i++) {

        String matdocItem = matdocItems[i]?.trim()

        String catchWeightValue =
            i < catchWeightValues.length
                ? catchWeightValues[i]?.trim()
                : null

        if (matdocItem && catchWeightValue) {
            try {
                int itemNumber = Integer.parseInt(matdocItem)
                catchWeightByItem.put(itemNumber, catchWeightValue)
            } catch (Exception ignored) {
                // Ignore invalid MATDOC_ITM
            }
        }
    }

    // Produce one output position for every standard item
    for (int i = 0; i < itemDriver.length; i++) {

        int generatedItemIndex = i + 1

        String result = catchWeightByItem.get(generatedItemIndex)

        if (result != null && result != "") {
            output.addValue(result)
        } else {
            output.addValue("")
        }
    }
}