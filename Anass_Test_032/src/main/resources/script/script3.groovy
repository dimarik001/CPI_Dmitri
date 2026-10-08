import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {

    String body = message.getBody(String)

    Integer splitIndex = message.getHeader("CamelSplitIndex", Integer.class)
    Integer splitSize  = message.getHeader("CamelSplitSize", Integer.class)

    if (body == null || body.trim().isEmpty()) {
        throw new Exception("Empty body. SplitIndex=${splitIndex}, SplitSize=${splitSize}")
    }

    return message
}