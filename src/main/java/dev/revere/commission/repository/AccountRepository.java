package dev.revere.commission.repository;

import dev.revere.commission.entities.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/25/2024
 */
public interface AccountRepository extends MongoRepository<Account, String> {
    /**
     * Find an account by its username.
     *
     * @param p_username The username to search for.
     * @return The account with the given username, if it exists.
     */
    Optional<Account> findByUsername(String p_username);
}
