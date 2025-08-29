package com.example.altteulmoa_project.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // 이거 환경 변수 에서 받아오느 키로 변경하기
    // 이거는 지금 랜덤으로 변하는 키값 생성
    // 지금 es256은 안되고 hs256이 되는 이유는 Keys.secretKeyFor()는
    // 대칭 키를 생성하는 메서드예요. 그런데 ES256은 비대칭 키 알고리즘이라서 이 메서드로는 키를 만들 수 없기때문에
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 만료시간
    private final long expiration = 1000L * 60 * 60 ; // 1시간

    // 토큰 생성
    public String generateToken (String username){
        return Jwts.builder()
                .setSubject(username) // 토큰에 이름추가
                .setIssuedAt(new Date()) // 토큰 생성 시간
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 만료시간
                .signWith(key) // 이거는 위에 만들어둔 키로 토큰을 생성 한다는 의미이다
                .compact();
    }

    // 토큰 복호화해서 내용 확인
    public String getUsernameFromToken(String token){
        //key를 통해서 내용을 복호화해서 확인
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    //  토큰 검증 = 우리가 발급한 토큰이 맞는지 또는 시간이 만료 되었는지
    public boolean validateToken(String token){
        try {
            // 만료가 되었는지 확인 = 토큰을 복호화 하지 못했다면 에러처리
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


}
