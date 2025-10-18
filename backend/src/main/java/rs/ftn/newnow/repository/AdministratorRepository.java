package rs.ftn.newnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Administrator;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
}
