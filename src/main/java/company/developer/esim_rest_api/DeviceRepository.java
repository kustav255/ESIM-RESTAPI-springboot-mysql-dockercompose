package company.developer.esim_rest_api;

import org.springframework.data.repository.CrudRepository;

public interface DeviceRepository extends CrudRepository<Device, Integer> {

    Iterable<Device> findByBrandContainingIgnoreCase(String brand);

    Iterable<Device> findByState(STATE state);
}
