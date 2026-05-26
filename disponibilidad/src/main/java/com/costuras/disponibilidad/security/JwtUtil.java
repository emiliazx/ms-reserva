package com.costuras.disponibilidad.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${jwt.secret}") private String secretKey;
    private SecretKey key;

    @PostConstruct
    public void init() { key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)); }

    public Claims getAllClaims(String t) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(t).getPayload(); }
    public <T> T getClaim(String t, Function<Claims, T> r) { return r.apply(getAllClaims(t)); }
    public boolean isTokenExpired(String t) { return getClaim(t, Claims::getExpiration).before(new Date()); }

    public UsuarioPrincipal getPrincipalFromToken(String t) {
        Claims c = getAllClaims(t);
        Integer id = null;
        Object idC = c.get("id");
        if (idC instanceof Integer) id = (Integer) idC;
        else if (idC instanceof Long) id = ((Long) idC).intValue();
        return UsuarioPrincipal.builder().id(id).username(c.getSubject()).role(c.get("role", String.class)).build();
    }
}
