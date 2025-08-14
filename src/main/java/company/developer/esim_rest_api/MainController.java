package company.developer.esim_rest_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(path="/devices")
public class MainController {

    @Autowired
    private DeviceRepository deviceRepository;

    @GetMapping
    public @ResponseBody Iterable<Device> getAllDevices(
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) STATE state) {

        // If brand is provided, filter by brand
        if (brand != null) {
            return deviceRepository.findByBrandContainingIgnoreCase(brand);
        }
        // If state is provided, filter by state
        else if (state != null) {
            return deviceRepository.findByState(state);
        }
        // If no filters are provided, return all devices
        return deviceRepository.findAll();
    }

    @PostMapping
    public @ResponseBody String addNewDevice(
            @RequestParam String name,
            @RequestParam String brand) {

        Device n = new Device();
        n.setName(name);
        n.setBrand(brand);
        n.setState(STATE.AVAILABLE);
        deviceRepository.save(n);
        return "Saved";
    }

    @GetMapping(path="/{id}")
    public @ResponseBody Optional<Device> getDeviceById(@PathVariable Integer id) {
        return deviceRepository.findById(id);
    }

    @PutMapping(path="/{id}")
    public @ResponseBody String updateDevice(
            @PathVariable Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand) {

        // If both name and brand are null, return the message below
        if(name == null && brand == null){
            return "No fields to update";
        }

        // Check if device exists
        Optional<Device> d = deviceRepository.findById(id);
        if(d.isPresent()){

            // Check state before update
            if(!d.get().getState().equals(STATE.INUSE)){

                // Update only the fields that are not null
                if (name != null) {
                    d.get().setName(name);
                }
                if (brand != null) {
                    d.get().setBrand(brand);
                }

                // Save the updated device
                deviceRepository.save(d.get());
                return "Updated";
            }
            return "Not updated. Device in-use";
        }
        return "Device ID not found";
    }

    // Update device state by ENUM
    @PatchMapping("/{id}")
    public @ResponseBody String updateDeviceState(
        @PathVariable Integer id,
        @RequestParam STATE state) {

        // Check if device exists and update its state
        Optional<Device> d = deviceRepository.findById(id);
        if (d.isPresent()) {
            d.get().setState(state);
            deviceRepository.save(d.get());
            return "Device state updated";
        }
        return "Device ID not found";
    }

    @DeleteMapping(path="/{id}")
    public @ResponseBody String deleteDevice(@PathVariable Integer id) {
        Optional<Device> d = deviceRepository.findById(id);
        if(d.isPresent()) {
            // Check state before delete
            // Only delete if the device is not in use
            if (!d.get().getState().equals(STATE.INUSE)) {
                deviceRepository.deleteById(id);
                return "Deleted";
            }
            return "Not deleted. Device in-use";
        }
        return "Device ID not found";
    }
}
