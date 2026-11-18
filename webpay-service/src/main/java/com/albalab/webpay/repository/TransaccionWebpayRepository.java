package com.albalab.webpay.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.albalab.webpay.model.TransaccionWebpay;

public interface TransaccionWebpayRepository extends JpaRepository<TransaccionWebpay, Long> {

    Optional<TransaccionWebpay> findByToken(String token);

    Optional<TransaccionWebpay> findByOrdenCompra(String ordenCompra);

    boolean existsByOrdenCompra(String ordenCompra);

    List<TransaccionWebpay> findAllByOrderByIdDesc();
}
