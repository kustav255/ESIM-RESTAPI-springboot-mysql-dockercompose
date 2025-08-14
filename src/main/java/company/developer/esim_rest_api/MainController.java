package company.developer.esim_rest_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/devices")
public class MainController {

    @Autowired
    private DeviceRepository deviceRepository;

    @GetMapping
    public @ResponseBody ResponseEntity<Iterable<Device>> getAllDevices(
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) STATE state) {

        // If brand is provided, filter by brand
        if (brand != null) {
            return ResponseEntity.ok(deviceRepository.findByBrandContainingIgnoreCase(brand));
        }
        // If state is provided, filter by state
        else if (state != null) {
            return ResponseEntity.ok(deviceRepository.findByState(state));
        }
        // If no filters are provided, return all devices
        return ResponseEntity.ok(deviceRepository.findAll());
    }

    @PostMapping
    public @ResponseBody ResponseEntity<String> addNewDevice(
            @RequestParam String name,
            @RequestParam String brand) {

        Device n = new Device();
        n.setName(name);
        n.setBrand(brand);
        n.setState(STATE.AVAILABLE);
        deviceRepository.save(n);
        return ResponseEntity.status(201).body("Saved");
    }

    @GetMapping(path="/{id}")
    public @ResponseBody ResponseEntity<Device> getDeviceById(@PathVariable Integer id) {
        return deviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(path="/{id}")
    public @ResponseBody ResponseEntity<String> updateDevice(
            @PathVariable Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand) {

        // If both name and brand are null, return the message below
        if(name == null && brand == null){
            return ResponseEntity.badRequest().body("No fields to update");
        }

        // Check if device exists and update its fields
        return deviceRepository.findById(id).map(device -> {
            // Only update if the device is not in use
            if (!device.getState().equals(STATE.INUSE)) {
                if (name != null) device.setName(name);
                if (brand != null) device.setBrand(brand);
                deviceRepository.save(device);
                return ResponseEntity.ok("Updated");
            }
            // If the device is in use, return a conflict (409) response
            return ResponseEntity.status(409).body("Not updated. Device in-use");
        }).orElse(ResponseEntity.notFound().build());
    }

    // Update device state by ENUM
    @PatchMapping("/{id}")
    public @ResponseBody ResponseEntity<String> updateDeviceState(
        @PathVariable Integer id,
        @RequestParam STATE state) {

        // Check if the device exists and update its state to the ENUM options
        // AVAILABLE, INUSE, INACTIVE
        return deviceRepository.findById(id).map(device -> {
            device.setState(state);
            deviceRepository.save(device);
            return ResponseEntity.ok("Device state updated to: " + state.getState());
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping(path="/{id}")
    public @ResponseBody ResponseEntity<String> deleteDevice(@PathVariable Integer id) {

        // Check if the device exists and delete it
        return deviceRepository.findById(id).map(device -> {
            // Only delete if the device is not in use
            if (!device.getState().equals(STATE.INUSE)) {
                deviceRepository.deleteById(id);
                return ResponseEntity.ok("Deleted");
            }
            return ResponseEntity.status(409).body("Not deleted. Device in-use");
        }).orElse(ResponseEntity.notFound().build());
    }
}
