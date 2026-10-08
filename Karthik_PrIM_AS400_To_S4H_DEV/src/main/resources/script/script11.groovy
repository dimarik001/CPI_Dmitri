import com.sap.gateway.ip.core.customdev.util.Message

Message processData(Message message){
    def body = message.getBody(String)
    body = body.replaceAll(/<\?xml.*?\?>/, "").trim()
    message.setBody(body)
    return message
}
