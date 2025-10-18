package rs.ftn.newnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Rate;

@Repository
public interface RateRepository extends JpaRepository<Rate, Long> {
}
