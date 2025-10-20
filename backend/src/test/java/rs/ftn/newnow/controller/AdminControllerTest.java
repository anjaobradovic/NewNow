package rs.ftn.newnow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.ftn.newnow.dto.AccountRequestDTO;
import rs.ftn.newnow.dto.AccountRequestPageResponse;
import rs.ftn.newnow.model.enums.RequestStatus;
import rs.ftn.newnow.service.AccountRequestService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountRequestService accountRequestService;

    private AccountRequestDTO accountRequestDTO;

    @BeforeEach
    void setUp() {
        accountRequestDTO = new AccountRequestDTO();
        accountRequestDTO.setId(1L);
        accountRequestDTO.setEmail("test@example.com");
        accountRequestDTO.setName("Test User");
        accountRequestDTO.setStatus(RequestStatus.PENDING);
        accountRequestDTO.setCreatedAt(LocalDate.now());
        accountRequestDTO.setAddress("Test Address");
        accountRequestDTO.setCity("Test City");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRegisterRequests_AllRequests() throws Exception {
        AccountRequestPageResponse pageResponse = new AccountRequestPageResponse(
                List.of(accountRequestDTO),
                0,
                10,
                1,
                1
        );

        when(accountRequestService.getFilteredRequests(isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/admin/register-requests")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRegisterRequests_FilterByStatus() throws Exception {
        AccountRequestPageResponse pageResponse = new AccountRequestPageResponse(
                List.of(accountRequestDTO),
                0,
                10,
                1,
                1
        );

        when(accountRequestService.getFilteredRequests(eq(RequestStatus.PENDING), isNull(), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/admin/register-requests")
                        .param("status", "pending")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRegisterRequests_FilterByQuery() throws Exception {
        AccountRequestPageResponse pageResponse = new AccountRequestPageResponse(
                List.of(accountRequestDTO),
                0,
                10,
                1,
                1
        );

        when(accountRequestService.getFilteredRequests(isNull(), eq("test"), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/admin/register-requests")
                        .param("q", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRegisterRequests_InvalidStatus() throws Exception {
        mockMvc.perform(get("/api/admin/register-requests")
                        .param("status", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRegisterRequests_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/register-requests"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRequest_Success() throws Exception {
        doNothing().when(accountRequestService).approveRequest(anyLong());

        mockMvc.perform(patch("/api/admin/register-requests/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account request approved successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRequest_NotFound() throws Exception {
        doThrow(new IllegalArgumentException("Account request not found with id: 999"))
                .when(accountRequestService).approveRequest(anyLong());

        mockMvc.perform(patch("/api/admin/register-requests/999/approve"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRequest_AlreadyProcessed() throws Exception {
        doThrow(new IllegalArgumentException("This request has already been processed"))
                .when(accountRequestService).approveRequest(anyLong());

        mockMvc.perform(patch("/api/admin/register-requests/1/approve"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This request has already been processed"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectRequest_Success() throws Exception {
        doNothing().when(accountRequestService).rejectRequest(anyLong());

        mockMvc.perform(patch("/api/admin/register-requests/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account request rejected successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectRequest_NotFound() throws Exception {
        doThrow(new IllegalArgumentException("Account request not found with id: 999"))
                .when(accountRequestService).rejectRequest(anyLong());

        mockMvc.perform(patch("/api/admin/register-requests/999/reject"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void approveRequest_Forbidden() throws Exception {
        mockMvc.perform(patch("/api/admin/register-requests/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void rejectRequest_Forbidden() throws Exception {
        mockMvc.perform(patch("/api/admin/register-requests/1/reject"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRegisterRequests_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/register-requests"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approveRequest_Unauthorized() throws Exception {
        mockMvc.perform(patch("/api/admin/register-requests/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectRequest_Unauthorized() throws Exception {
        mockMvc.perform(patch("/api/admin/register-requests/1/reject"))
                .andExpect(status().isForbidden());
    }
}
