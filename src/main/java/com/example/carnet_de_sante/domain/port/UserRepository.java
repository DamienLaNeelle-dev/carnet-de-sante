package com.example.carnet_de_sante.domain.port;

import com.example.carnet_de_sante.domain.model.Role;
import com.example.carnet_de_sante.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    boolean existsByEmail(String email);
}
