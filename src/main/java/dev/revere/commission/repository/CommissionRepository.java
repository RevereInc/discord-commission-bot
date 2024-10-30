package dev.revere.commission.repository;

import dev.revere.commission.entities.Commission;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Commission entities in MongoDB.
 * This interface provides methods for CRUD operations on Commission objects.
 *
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
public interface CommissionRepository extends MongoRepository<Commission, String> {
    /**
     * Find a commission by the commission ID.
     *
     * @param id The ID of the commission.
     * @return The Commission object associated with the given ID.
     */
    Commission findCommissionById(@NotNull String id);

    /**
     * Find a commission by the channel ID.
     *
     * @param channelId The ID of the channel associated with the commission.
     * @return The Commission object associated with the given channel ID.
     */
    Commission findCommissionByChannelId(long channelId);

    /**
     * Find a commission by the public channel ID.
     *
     * @param publicChannelId The ID of the channel associated with the public commission.
     * @return The Commission object associated with the given channel ID:
     */
    Commission findCommissionByPublicChannelId(long publicChannelId);

    /**
     * Find a commission by the user ID.
     *
     * @param userId The ID of the user associated with the commission.
     * @return The Commission object associated with the given user ID.
     */
    Commission findCommissionByUserId(long userId);

    /**
     * Find a commission by the invoice ID.
     *
     * @param invoiceId The ID of the invoice associated with the commission.
     * @return The Commission object associated with the given invoice ID.
     */
    Optional<Commission> findCommissionByInvoiceId(String invoiceId);

    /**
     * Find a commission by the Stripe payment link ID.
     *
     * @param stripePaymentLinkId The ID of the payment link associated with the commission.
     * @return The Commission object associated with the given payment link ID.
     */
    Optional<Commission> findCommissionByStripePaymentLinkId(String stripePaymentLinkId);

    /**
     * Retrieve a list of commissions ordered by creation date in descending order.
     *
     * @param pageable The pagination information.
     * @return A list of commissions ordered by creation date in descending order.
     */
    List<Commission> findByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Retrieve the total amount of all commissions
     *
     * @return The count of all commissions
     */
    long count();
}