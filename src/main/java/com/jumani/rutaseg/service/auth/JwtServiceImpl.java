package com.jumani.rutaseg.service.auth;

import com.jumani.rutaseg.util.DateGen;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class JwtServiceImpl implements JwtService, DateGen {

    private final String secretKey;

    public JwtServiceImpl(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String generateToken(String subject, boolean admin) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("admin", admin);
        return this.createToken(claims, subject);
    }

    @Override
    public String extractSubject(String token) {
        return this.extractAllClaims(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("could not validate token", e);
            return false;
        }
    }

    @Override
    public boolean isAdminToken(String token) {
        final Claims claims = this.extractAllClaims(token);
        return claims.get("admin").equals(true);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        final ZonedDateTime now = this.currentDateUTC();

        final Date issuedAt = Date.from(now.toInstant());
        final Date expirationDate = Date.from(now.plusHours(3).toInstant());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    private Date extractExpiration(String token) {
        return this.extractAllClaims(token).getExpiration();
    }

    /**
     * Claims es un mapa clave valor con los distintos datos encriptados en el jwt
     * Tiene claves fijas.
     * En nuestro caso guardamos la expiración como EXPIRATION y el uid como SUBJECT
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    /**
     * Le sumamos un minuto a la fecha actual,
     * ya que luego de validar el JWT otros servicios lo consumen.
     * Entonces nos aseguramos que sea válido ahora y de acá a un minuto en el futuro.
     */
    private boolean isTokenExpired(String token) {
        final Date nowPlusDelta = Date.from(this.currentDateUTC().plusMinutes(1).toInstant());
        return extractExpiration(token).before(nowPlusDelta);
    }
}

