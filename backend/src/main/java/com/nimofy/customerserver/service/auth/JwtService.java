package com.nimofy.customerserver.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimofy.customerserver.dto.google.GoogleKey;
import com.nimofy.customerserver.dto.google.GoogleKeysList;
import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.util.CryptographyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    public static final String ROLES = "roles";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String AZP = "azp";
    private static final String GOOGLE_CERTS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String KID = "kid";

    @Value("${token.expiration.time}")
    private Long TEN_MINUTES_IN_SECONDS;
    @Value("${backend.api.url}")
    private String backendUrl;
    @Value("${google.default.n}")
    private String googleDefaultNValue;
    @Value("${google.default.e}")
    private String googleDefaultEValue;
    @Value("${google.client.id}")
    private String googleClientId;

    private final NimbusJwtEncoder jwtEncoder;
    private final RestTemplate restTemplate;
    private final CryptographyUtil cryptographyUtil;

    public Jwt createToken(User user) {
        Instant now = Instant.now();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwtClaimsSet.builder()
                .issuer(backendUrl)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(TEN_MINUTES_IN_SECONDS))
                .subject(String.format("%s,%s,%s", user.getId(), user.getWalletAddress(), user.getPassword()))
                .claim(ROLES, user.getAuthority().name())
                .build())
        );
    }

    public String getPassword(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String subjectClaim = signedJWT.getJWTClaimsSet().getSubject();
            String[] subjectClaims = subjectClaim.split(",");
            if (subjectClaims.length < 2) return null;
            return subjectClaims[2];
        } catch (Exception e) {
            log.error("could not get password with tokenSalt out of token", e);
            return null;
        }
    }

    public boolean verifyGoogleToken(String googleToken) {
        try {
            String[] splitGoogleToken = googleToken.split("\\.");
            if (splitGoogleToken.length != 3) {
                log.info("google token has invalid jwt format");
                return false;
            }
            String tokenHeader = splitGoogleToken[0];
            String googleTokenKid = retrieveKid(tokenHeader);
            GoogleKey googleKey = retreiveGoogleKey(googleTokenKid);

            JWK jwk = new RSAKey.Builder(new Base64URL(googleKey.n()), new Base64URL(googleKey.e())).build();
            JWSVerifier verifier = new RSASSAVerifier(jwk.toRSAKey());
            SignedJWT signedJWT = SignedJWT.parse(googleToken);

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            String azp = claims.getStringClaim(AZP);
            List<String> audience = claims.getAudience();
            return signedJWT.verify(verifier) && StringUtils.equals(googleClientId, azp) &&
                    cryptographyUtil.isAudienceValid(audience, googleClientId) &&
                    cryptographyUtil.isEmailVerified(claims);
        } catch (Exception e) {
            log.error("could not verify google token [{}]", googleToken, e);
            return false;
        }
    }

    private GoogleKey retreiveGoogleKey(String kid) {
        GoogleKey defaultGoogleKey = new GoogleKey(googleDefaultEValue, kid, googleDefaultNValue);
        ResponseEntity<GoogleKeysList> googleKeysListResponseEntity = restTemplate.getForEntity(GOOGLE_CERTS_URL, GoogleKeysList.class);
        if (!googleKeysListResponseEntity.getStatusCode().is2xxSuccessful() || googleKeysListResponseEntity.getBody() == null) {
            log.error("could not fetch google keys or response is malformed using default key");
            return defaultGoogleKey;
        }
        return googleKeysListResponseEntity.getBody().keys()
                .stream()
                .filter(googleKey -> googleKey.kid().equalsIgnoreCase(kid))
                .findFirst()
                .orElse(defaultGoogleKey);
    }

    private String retrieveKid(String tokenHeader) throws IOException {
        JsonNode node = OBJECT_MAPPER.readValue(Base64.getUrlDecoder().decode(tokenHeader), JsonNode.class);
        return node.get(KID).asText();
    }
}