package rs.ftn.newnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
