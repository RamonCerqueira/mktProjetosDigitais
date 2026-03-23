package com.mktplace.repository;

import com.mktplace.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByOfferIdOrderByCreatedAtAsc(Long offerId);
}
