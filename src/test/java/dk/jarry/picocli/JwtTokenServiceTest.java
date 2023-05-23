package dk.jarry.picocli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jose4j.base64url.Base64;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

@QuarkusTest
public class JwtTokenServiceTest {

    @Inject
    private JwtTokenService jwtTokenService;

    @Test
    void generateToken() {
        String token = jwtTokenService.generateToken(ORGINAL_KID, Base64.encode(ORGINAL_PAYLOAD.getBytes()),0);
        assertNotNull(token);

        var partsAsJsonObject = jwtTokenService.getJsonObjects(token);
        JsonObject header = partsAsJsonObject.get(0);
        JsonObject payload = partsAsJsonObject.get(1);

        assertEquals(ORGINAL_KID, header.getString("kid"));
        assertEquals("Michael Bornholdt Nielsen", payload.getString("name"));

    }

    private final static String ORGINAL_KID = "ucJxucWD1VfqR5NzBkJfx6FsYbbGxG18y9mVkk1XabY";
    private final static String ORGINAL_PAYLOAD = """
            {
                "exp": 1684758420,
                "iat": 1684758120,
                "iss": "https://keycloak.jarry.dk:8543/realms/jarry",
                "aud": "account",
                "sub": "bb93efa1-ebe8-4443-a9d5-8765a5ae5cbe",
                "typ": "Bearer",
                "azp": "todo-service-hack",
                "acr": "1",
                "allowed-origins": [
                  "*"
                ],
                "realm_access": {
                  "roles": [
                    "default-roles-jarry",
                    "offline_access",
                    "uma_authorization",
                    "user"
                  ]
                },
                "resource_access": {
                  "account": {
                    "roles": [
                      "manage-account",
                      "manage-account-links",
                      "view-profile"
                    ]
                  }
                },
                "scope": "email profile",
                "sid": "0df83c2a-a70f-4ff4-9988-3f93d9bd78ff",
                "email_verified": true,
                "name": "Michael Bornholdt Nielsen",
                "preferred_username": "micbn",
                "given_name": "Michael Bornholdt",
                "family_name": "Nielsen",
                "email": "micbn@jarry.dk"
              }
                """;

}
