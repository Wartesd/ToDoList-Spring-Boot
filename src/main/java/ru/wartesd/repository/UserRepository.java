package ru.wartesd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.wartesd.entity.User;
import ru.wartesd.entity.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
     List <User> findAllByRoleInOrderById(Iterable<UserRole> roles);
     Optional<User> findByEmailIgnoreCase(String email);

     @Modifying
     @Query("UPDATE User SET role = :role WHERE id = :id")
     void updateRole(int id,@Param("role") UserRole newRole);
}
