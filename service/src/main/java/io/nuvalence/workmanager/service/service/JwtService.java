package io.nuvalence.workmanager.service.service;

import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

/**
 * Service layer to manage JSON Web Tokens .
 */
@Component
public class JwtService {

    /**
     * Returns mapped object from passed JWT object.
     * @param token Token to map object from HTTP request
     * @return Mapped JWT object
     */
    public Map<String,Object> getObjectMapFromHeader(String token) {
        try {
            if (token.contains("Bearer ")) {
                token = token.replace("Bearer ", "");
            }

            String payload = this.getPayloadFromJwt(token);
            return JsonParserFactory.getJsonParser().parseMap(payload);
        } catch (ArrayIndexOutOfBoundsException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Unable to parse JWT Object from header; Invalid token", e);
        } catch (NullPointerException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Unable to parse JWT Object from header; Token cannot be null.", e);
        }
    }

    private String getPayloadFromJwt(String token) {

        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));
        return payload;
    }
}
