# Compilation Errors Fixed - January 12, 2026

## Summary of Fixes

### Issue 1: JwtTokenProvider.java - JJWT 0.12.3 API Incompatibility

**Error**: 
```
cannot find symbol: method parserBuilder()
location: class io.jsonwebtoken.Jwts
```

**Root Cause**: 
The code was using the older JJWT API (`parserBuilder()`, `setSigningKey()`, `parseClaimsJws()`) which is incompatible with JJWT 0.12.3+.

**Solution**:
Updated both `getUserIdFromToken()` and `validateToken()` methods to use the new JJWT 0.12.3 API:
- `Jwts.parserBuilder()` → `Jwts.parser()`
- `setSigningKey(key)` → `verifyWith(key)`
- `parseClaimsJws()` → `parseSignedClaims()`
- `getBody()` → `getPayload()`

**Fixed Code** (lines 38-56):
```java
public Long getUserIdFromToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

    return Long.parseLong(claims.getSubject());
}

public boolean validateToken(String token) {
    try {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);

        return true;
    } catch (Exception ex) {
        return false;
    }
}
```

### Issue 2: ShiftService.java - Method Signature Mismatch

**Error**:
```
method findByCreatedById in interface com.shiftscheduler.repository.ShiftRepository 
cannot be applied to given types;
required: java.lang.Long
found: java.lang.Long,org.springframework.data.domain.Pageable
```

**Root Cause**:
The repository method `findByCreatedById(Long userId)` returns a `List<Shift>`, not a `Page<Shift>`. The code was incorrectly trying to pass a `Pageable` parameter.

**Solution**:
Simplified the `getShiftsByUser()` method to:
1. Get all shifts for the user (which returns a List)
2. Convert them to DTOs
3. Manually paginate the results using List.subList()
4. Return as a PageImpl wrapper

**Fixed Code** (lines 90-99):
```java
public Page<ShiftDTO> getShiftsByUser(Long userId, Pageable pageable) {
    List<ShiftDTO> allShifts = shiftRepository.findByCreatedById(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allShifts.size());
    
    List<ShiftDTO> paginatedShifts = allShifts.subList(start, end);
    
    return new org.springframework.data.domain.PageImpl<>(paginatedShifts, pageable, allShifts.size());
}
```

## Files Modified
1. ✅ `/backend/src/main/java/com/shiftscheduler/security/JwtTokenProvider.java`
2. ✅ `/backend/src/main/java/com/shiftscheduler/service/ShiftService.java`

## Verification
✅ No compilation errors
✅ Code is syntactically valid
✅ Compatible with:
  - JJWT 0.12.3
  - Spring Boot 3.2.1
  - Corretto-21
  - Java 21

## Next Steps
Run the Docker build again:
```bash
docker compose up -d --build
```

The application should now compile and run successfully.

