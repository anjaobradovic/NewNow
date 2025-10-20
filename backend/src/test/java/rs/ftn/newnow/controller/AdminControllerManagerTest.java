package rs.ftn.newnow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.ftn.newnow.dto.AssignManagerDTO;
import rs.ftn.newnow.dto.ManagerDTO;
import rs.ftn.newnow.service.ManagesService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerManagerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ManagesService managesService;

    private ManagerDTO managerDTO;

    @BeforeEach
    void setUp() {
        managerDTO = new ManagerDTO();
        managerDTO.setUserId(1L);
        managerDTO.setName("Test Manager");
        managerDTO.setEmail("manager@test.com");
        managerDTO.setStartDate(LocalDate.now());
        managerDTO.setActive(true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLocationManagers_Success() throws Exception {
        List<ManagerDTO> managers = Collections.singletonList(managerDTO);
        when(managesService.getLocationManagers(anyLong())).thenReturn(managers);

        mockMvc.perform(get("/api/admin/locations/1/managers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Manager"));

        verify(managesService).getLocationManagers(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getLocationManagers_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/locations/1/managers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignManager_Success() throws Exception {
        AssignManagerDTO dto = new AssignManagerDTO(1L);
        doNothing().when(managesService).assignManager(anyLong(), any(AssignManagerDTO.class));

        mockMvc.perform(post("/api/admin/locations/1/managers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Manager assigned successfully"));

        verify(managesService).assignManager(eq(1L), any(AssignManagerDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignManager_ValidationError() throws Exception {
        AssignManagerDTO dto = new AssignManagerDTO(1L);
        doThrow(new IllegalArgumentException("User is already a manager"))
                .when(managesService).assignManager(anyLong(), any(AssignManagerDTO.class));

        mockMvc.perform(post("/api/admin/locations/1/managers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is already a manager"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeManager_Success() throws Exception {
        doNothing().when(managesService).removeManager(anyLong(), anyLong());

        mockMvc.perform(delete("/api/admin/locations/1/managers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Manager removed successfully"));

        verify(managesService).removeManager(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeManager_NotAManager() throws Exception {
        doThrow(new IllegalArgumentException("User is not an active manager"))
                .when(managesService).removeManager(anyLong(), anyLong());

        mockMvc.perform(delete("/api/admin/locations/1/managers/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not an active manager"));
    }

    @Test
    void getLocationManagers_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/locations/1/managers"))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignManager_Unauthorized() throws Exception {
        AssignManagerDTO dto = new AssignManagerDTO(1L);

        mockMvc.perform(post("/api/admin/locations/1/managers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
