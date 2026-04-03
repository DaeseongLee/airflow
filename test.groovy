import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.net.HttpURLConnection
import java.net.URL

// NiFi API 접속 정보
def nifiHost = 'http://localhost:8080'  // NiFi 주소 변경
def controllerServiceId = 'your-controller-service-id' // 수정할 Controller Service ID

// 인증 정보가 필요하면 헤더에 추가 (Bearer 토큰 예시)
// def authToken = 'Bearer YOUR_TOKEN'

// Controller Service 정보 조회 함수
def getControllerService(String id) {
    URL url = new URL("$nifiHost/nifi-api/controller-services/$id")
    HttpURLConnection conn = (HttpURLConnection) url.openConnection()
    conn.setRequestMethod("GET")
    conn.setRequestProperty("Accept", "application/json")
    // conn.setRequestProperty("Authorization", authToken) // 인증 필요 시

    if (conn.responseCode == 200) {
        def response = conn.inputStream.text
        return new JsonSlurper().parseText(response)
    } else {
        throw new RuntimeException("Failed : HTTP error code : ${conn.responseCode}")
    }
}

// Controller Service 수정 함수
def updateControllerService(Map controllerServiceData) {
    URL url = new URL("$nifiHost/nifi-api/controller-services/${controllerServiceData.id}")
    HttpURLConnection conn = (HttpURLConnection) url.openConnection()
    conn.setRequestMethod("PUT")
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setDoOutput(true)
    // conn.setRequestProperty("Authorization", authToken) // 인증 필요 시

    def jsonBody = JsonOutput.toJson(controllerServiceData)
    conn.outputStream.withWriter("UTF-8") { writer -> writer.write(jsonBody) }

    if (conn.responseCode == 200) {
        def response = conn.inputStream.text
        return new JsonSlurper().parseText(response)
    } else {
        throw new RuntimeException("Failed : HTTP error code : ${conn.responseCode}")
    }
}

try {
    // 1. 현재 Controller Service 정보 가져오기
    def cs = getControllerService(controllerServiceId)
    println "Before update:"
    println JsonOutput.prettyPrint(JsonOutput.toJson(cs))

    // 2. 수정할 내용 반영 (예: 이름 변경)
    cs.component.name = "새로운 이름"
    // 버전 관리용 revision 필드도 반드시 포함
    cs.revision.clientId = "my-client-id" // 클라이언트 고유 ID로 변경
    cs.revision.version = cs.revision.version  // 기존 버전 유지

    // 3. 수정된 Controller Service 업데이트 요청
    def updatedCs = updateControllerService(cs)
    println "After update:"
    println JsonOutput.prettyPrint(JsonOutput.toJson(updatedCs))

} catch (Exception e) {
    println "Error: ${e.message}"
}