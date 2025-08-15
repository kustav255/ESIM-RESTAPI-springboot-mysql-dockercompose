// File: src/test/java/company/developer/esim_rest_api/MainControllerTest.java
package company.developer.esim_rest_api;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 FUTURE FEATURES:
 - Remove the Mock, and start using the real DeviceRepository with an in-memory database like H2.
 - Add more tests for edge cases and error handling.
 - Add TestContainers for containers and integration tests
 */
@WebMvcTest(MainController.class)
@Import(MainControllerTest.MockConfig.class)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepository;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public DeviceRepository deviceRepository() {
            return Mockito.mock(DeviceRepository.class);
        }
    }

    // Test getAllDevices endpoint without filters
    @Test
    void getAllDevicesReturnsOk() throws Exception {
        when(deviceRepository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/devices"))
                .andExpect(status().isOk());
    }

    // Test getAllDevices endpoint with brand filter
    @Test
    void getAllDevicesWithBrandFilter() throws Exception {
        when(deviceRepository.findByBrandContainingIgnoreCase("Apple"))
                .thenReturn(Collections.singletonList(new Device()));
        mockMvc.perform(get("/devices?brand=Apple"))
                .andExpect(status().isOk());
    }

    // Test getAllDevices endpoint with state filter
    @Test
    void getAllDevicesWithStateFilter() throws Exception {
        when(deviceRepository.findByState(STATE.AVAILABLE))
                .thenReturn(Collections.singletonList(new Device()));
        mockMvc.perform(get("/devices?state=AVAILABLE"))
                .andExpect(status().isOk());
    }

    // Test addNewDevice endpoint with valid parameters
    @Test
    void addNewDeviceReturnsCreated() throws Exception {
        when(deviceRepository.save(any(Device.class))).thenReturn(new Device());
        mockMvc.perform(post("/devices")
                        .param("name", "iPhone 14")
                        .param("brand", "Apple"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Saved"));
    }

    // Test getDeviceById endpoint with valid ID
    @Test
    void getDeviceByIdFound() throws Exception {
        Device device = new Device();
        device.setId(1);
        when(deviceRepository.findById(1)).thenReturn(Optional.of(device));
        mockMvc.perform(get("/devices/1"))
                .andExpect(status().isOk());
    }

    // Test getDeviceById endpoint with non-existing ID
    @Test
    void getDeviceByIdNotFound() throws Exception {
        when(deviceRepository.findById(1)).thenReturn(Optional.empty());
        mockMvc.perform(get("/devices/1"))
                .andExpect(status().isNotFound());
    }

    // Test updateDevice endpoint with valid parameters
    @Test
    void updateDeviceSuccess() throws Exception {
        Device device = new Device();
        device.setId(1);
        device.setState(STATE.AVAILABLE);
        when(deviceRepository.findById(1)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);
        mockMvc.perform(put("/devices/1")
                        .param("name", "NewName"))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated"));
    }

    // Test updateDevice endpoint without fields provided
    @Test
    void updateDeviceNoFields() throws Exception {
        Device device = new Device();
        device.setId(1);
        device.setState(STATE.AVAILABLE);
        when(deviceRepository.findById(1)).thenReturn(Optional.of(device));
        mockMvc.perform(put("/devices/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No fields to update"));
    }

    // Test updateDevice endpoint with device state set to in-use
    @Test
    void updateDeviceInUse() throws Exception {
        Device device = new Device();
        device.setId(1);
        device.setState(STATE.INUSE);
        when(deviceRepository.findById(1)).thenReturn(Optional.of(device));
        mockMvc.perform(put("/devices/1")
                        .param("name", "NewName"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Not updated. Device in-use"));
    }

    // Test updateDevice endpoint with non-existing ID
    @Test
    void updateDeviceNotFound() throws Exception {
        when(deviceRepository.findById(1)).thenReturn(Optional.empty());
        mockMvc.perform(put("/devices/1")
                        .param("name", "NewName"))
                .andExpect(status().isNotFound());
    }

    // Test updateDeviceState endpoint with valid state change
    @Test
    void updateDeviceStateSuccess() throws Exception {
        Device device = new Device();
        device.setId(1);
        device.setState(STATE.AVAILABLE);
        when(deviceRepository.findById(1)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);
        mockMvc.perform(patch("/devices/1")
                        .param("state", "INUSE"))
                .andExpect(status().isOk())
                //.andExpect(content().string("Device state updated to: INUSE"));
                .andExpect(content().string("Device state updated to: in-use"));
    }

    // Test updateDeviceState endpoint with invalid ID
    @Test
    void updateDeviceStateNotFound() throws Exception {
        when(deviceRepository.findById(1)).thenReturn(Optional.empty());
        mockMvc.perform(patch("/devices/1")
                        .param("state", "INUSE"))
                .andExpect(status().isNotFound());
    }

    // Test deleteDevice endpoint with valid ID and available state
    @Test
    void deleteDeviceSuccess() throws Exception {
        Device device = new Device();
        device.setId(1);
        device.setState(STATE.AVAILABLE);
        when(deviceRepository.findById(1)).thenReturn(Optional.of(device));
        mockMvc.perform(delete("/devices/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }

    // Test deleteDevice endpoint with device in use
    @Test
    void deleteDeviceInUse() throws Exception {
        Device device = new Device();
        device.setId(1);
        device.setState(STATE.INUSE);
        when(deviceRepository.findById(1)).thenReturn(Optional.of(device));
        mockMvc.perform(delete("/devices/1"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Not deleted. Device in-use"));
    }

    // Test deleteDevice endpoint with non-existing ID
    @Test
    void deleteDeviceNotFound() throws Exception {
        when(deviceRepository.findById(1)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/devices/1"))
                .andExpect(status().isNotFound());
    }
}