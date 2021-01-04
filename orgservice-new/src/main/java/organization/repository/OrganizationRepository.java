package organization.repository;

import organization.model.Organization;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends CrudRepository<Organization,String>  {
    @Override
    public Optional<Organization> findById(String organizationId);
}
