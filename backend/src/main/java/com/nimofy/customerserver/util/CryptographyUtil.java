package com.nimofy.customerserver.util;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.security.SignatureException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
public class CryptographyUtil {

    private static final String UTC = "UTC";
    private static final String EMAIL = "email";
    private static final String EMAIL_VERIFIED = "email_verified";

    public String getAddressUsedToSignHashedMessage(String signedMessageInHex, String originalMessage) {
        if (signedMessageInHex.startsWith("0x")) {
            signedMessageInHex = signedMessageInHex.substring(2);
        }

        // No need to prepend these strings with 0x because
        // Numeric.hexStringToByteArray() accepts both formats
        String r = signedMessageInHex.substring(0, 64);
        String s = signedMessageInHex.substring(64, 128);
        String v = signedMessageInHex.substring(128, 130);

        // Using Sign.signedPrefixedMessageToKey for EIP-712 compliant signatures.
        try {
            String pubkey = Sign.signedPrefixedMessageToKey(originalMessage.getBytes(), new Sign.SignatureData(Numeric.hexStringToByteArray(v)[0], Numeric.hexStringToByteArray(r), Numeric.hexStringToByteArray(s))).toString(16);
            return Keys.getAddress(pubkey);
        } catch (SignatureException e) {
            log.error("could derive address from signed message");
            return null;
        }
    }

    public boolean isAudienceValid(List<String> audience, String googleClientId) {
        return audience != null && audience.size() >= 1 && audience.contains(googleClientId);
    }

    public boolean isEmailVerified(JWTClaimsSet claims) throws ParseException {
        String userGoogleEmail = claims.getStringClaim(EMAIL);
        Boolean emailVerified = claims.getBooleanClaim(EMAIL_VERIFIED);
        return userGoogleEmail != null && emailVerified;
    }

    public boolean isNotExpired(JWTClaimsSet claims) {
        return LocalDateTime.now().isBefore(convertToExpirationTime(claims));
    }

    private LocalDateTime convertToExpirationTime(JWTClaimsSet claims) {
        long expirationTimeMillis = claims.getExpirationTime().getTime();
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(expirationTimeMillis),
                ZoneId.of(UTC)
        );
    }
}