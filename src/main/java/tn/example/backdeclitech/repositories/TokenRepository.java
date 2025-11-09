package tn.example.backdeclitech.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.example.backdeclitech.entities.Token;
import tn.example.backdeclitech.entities.TokenType;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token,Integer> {
    @Query( "select t from Token t inner join User u on t.user.id = u.id where u.id = :id  and (t.expired = false or t.revoked = false)")
    List<Token> findAllValidTokenByUser(@Param("id") long id);

    @Query( "select t from Token t inner join User u on t.user.id = u.id where u.id = :id and t.tokenType = :tokenType and (t.expired = false or t.revoked = false)")
    List<Token> findAllValidTokenByUserAndType(@Param("id") long id, @Param("tokenType") TokenType tokenType);

    Optional<Token> findByToken(String token);
    
    Optional<Token> findByTokenAndTokenType(String token, TokenType tokenType);
}
