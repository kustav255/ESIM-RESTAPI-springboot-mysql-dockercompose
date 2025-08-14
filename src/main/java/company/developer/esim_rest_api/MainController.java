package company.developer.esim_rest_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping(path="/devices")
public class MainController {

    @Autowired
    private DeviceRepository deviceRepository;

    @PostMapping(path="/add") // Map ONLY POST Requests
    public @ResponseBody String addNewDevice(@RequestParam String name
            , @RequestParam String brand) {
        Device n = new Device();
        n.setName(name);
        n.setBrand(brand);
        n.setState(STATE.AVAILABLE);
        deviceRepository.save(n);
        return "Saved";
    }

    @GetMapping(path="/")
    public @ResponseBody Iterable<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @GetMapping(path="/id/{id}")
    public @ResponseBody Optional<Device> getDeviceById(@PathVariable Integer id) {
        return deviceRepository.findById(id);
    }

    @GetMapping(path="/brand/{brand}")
    public @ResponseBody Iterable<Device> getDevicesByBrand(@PathVariable String brand) {
        return deviceRepository.findByBrandContainingIgnoreCase(brand);
    }

    @GetMapping(path="/state/{state}")
    public @ResponseBody Iterable<Device> getDevicesByBrand(@PathVariable STATE state) {
        return deviceRepository.findByState(state);
    }


    @PatchMapping(path="/id/{id}")
    public @ResponseBody String updateDevice(@PathVariable Integer id, @RequestBody Device device) {
        // Check if device exists
        Optional<Device> d = deviceRepository.findById(id);
        if(d.isPresent()){
            // Check state before update
            if(!d.get().getState().equals(STATE.INUSE)){
               deviceRepository.save(device);
                return "Updated";
            }
            return "Not updated. Device in-use";
        }
        return "Device ID not found";
    }

    @DeleteMapping(path="/id/{id}")
    public @ResponseBody String deleteDevice(@PathVariable Integer id) {
        Optional<Device> d = deviceRepository.findById(id);
        if(d.isPresent()) {
            // Check state before update
            if (!d.get().getState().equals(STATE.INUSE)) {
                deviceRepository.deleteById(id);
                return "Deleted";
            }
            return "Not deleted. Device in-use";
        }
        return "Device ID not found";
    }


}
