package com.library.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.backend.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/test-data/borrow-record-test-data.sql")
@Transactional
class BorrowRecordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtUtils.generateToken("admin", 1L);
        userToken = jwtUtils.generateToken("testuser", 2L);
    }

    @Test
    void testGetAllBorrowRecordsAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/borrow/list")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        assertEquals(5, jsonNode.path("data").path("total").asInt());
    }

    @Test
    void testGetAllBorrowRecordsWithPagination() throws Exception {
        mockMvc.perform(get("/api/borrow/list")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.records.size()").value(2));
    }

    @Test
    void testGetAllBorrowRecordsWithUsernameFilter() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/borrow/list")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("size", "10")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        assertEquals(3, jsonNode.path("data").path("total").asInt());
    }

    @Test
    void testGetAllBorrowRecordsAsUserShouldFail() throws Exception {
        mockMvc.perform(get("/api/borrow/list")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void testGetMyBorrowRecordsAsUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/borrow/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        assertEquals(3, jsonNode.path("data").path("total").asInt());
    }

    @Test
    void testGetMyBorrowRecordsPermissionIsolation() throws Exception {
        MvcResult userResult = mockMvc.perform(get("/api/borrow/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode userNode = objectMapper.readTree(userResult.getResponse().getContentAsString());
        assertEquals(3, userNode.path("data").path("total").asInt());

        MvcResult adminResult = mockMvc.perform(get("/api/borrow/my")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode adminNode = objectMapper.readTree(adminResult.getResponse().getContentAsString());
        assertEquals(2, adminNode.path("data").path("total").asInt());
    }

    @Test
    void testReturnBook() throws Exception {
        MvcResult beforeResult = mockMvc.perform(get("/api/borrow/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andReturn();
        JsonNode beforeNode = objectMapper.readTree(beforeResult.getResponse().getContentAsString());
        long borrowedCountBefore = 0;
        for (JsonNode record : beforeNode.path("data").path("records")) {
            if (record.path("status").asInt() == 0) {
                borrowedCountBefore++;
            }
        }
        assertEquals(2, borrowedCountBefore);

        mockMvc.perform(post("/api/borrow/return/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        MvcResult afterResult = mockMvc.perform(get("/api/borrow/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andReturn();
        JsonNode afterNode = objectMapper.readTree(afterResult.getResponse().getContentAsString());
        long borrowedCountAfter = 0;
        for (JsonNode record : afterNode.path("data").path("records")) {
            if (record.path("status").asInt() == 0) {
                borrowedCountAfter++;
            }
        }
        assertEquals(1, borrowedCountAfter);
    }

    @Test
    void testReturnBookUpdatesStock() throws Exception {
        MvcResult bookBefore = mockMvc.perform(get("/api/books/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn();
        JsonNode bookBeforeNode = objectMapper.readTree(bookBefore.getResponse().getContentAsString());
        int stockBefore = bookBeforeNode.path("data").path("stock").asInt();

        mockMvc.perform(post("/api/borrow/return/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult bookAfter = mockMvc.perform(get("/api/books/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn();
        JsonNode bookAfterNode = objectMapper.readTree(bookAfter.getResponse().getContentAsString());
        int stockAfter = bookAfterNode.path("data").path("stock").asInt();

        assertEquals(stockBefore + 1, stockAfter);
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/borrow/list")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testReturnBookWithInvalidId() throws Exception {
        mockMvc.perform(post("/api/borrow/return/99999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("借阅记录不存在"));
    }

    @Test
    void testReturnAlreadyReturnedBook() throws Exception {
        mockMvc.perform(post("/api/borrow/return/3")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("该图书已归还"));
    }

    @Test
    void testReturnOtherUsersBookAsUser() throws Exception {
        mockMvc.perform(post("/api/borrow/return/4")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("无权操作他人的借阅记录"));
    }

    @Test
    void testReturnOtherUsersBookAsAdmin() throws Exception {
        mockMvc.perform(post("/api/borrow/return/2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));
    }
}
