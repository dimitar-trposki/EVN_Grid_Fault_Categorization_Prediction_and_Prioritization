package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.constants.JwtConstants;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtHelper {

    private SecretKey getSignIn() {
        byte[] keyBytes = Decoders.BASE64.decode(JwtConstants.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignIn())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims allClaims = extractAllClaims(token);
        return claimsResolver.apply(allClaims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String subject,
            Long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JwtConstants.EXPIRATION_TIME))
                .signWith(getSignIn())
                .compact();
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", userDetails.getAuthorities());
        return buildToken(extraClaims, userDetails.getUsername(), JwtConstants.EXPIRATION_TIME);
    }

    private boolean isExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return !isExpired(token) && username.equals(userDetails.getUsername());
    }
}
