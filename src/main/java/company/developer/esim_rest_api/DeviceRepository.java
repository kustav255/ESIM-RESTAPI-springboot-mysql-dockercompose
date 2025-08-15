package company.developer.esim_rest_api;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends CrudRepository<Device, Integer> {

    Iterable<Device> findByBrandContainingIgnoreCase(String brand);

    Iterable<Device> findByState(STATE state);
}
