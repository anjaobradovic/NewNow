package rs.ftn.newnow.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.search.index.LocationIndex;

@Repository
public interface LocationIndexRepository extends ElasticsearchRepository<LocationIndex, String> {
}
