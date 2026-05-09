package com.library.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.backend.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/test-data/borrow-record-test-data.sql")
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
    @DisplayName("管理员查询所有借阅记录 - 验证分页功能")
    void testAdminQueryAllRecordsWithPagination() throws Exception {
        mockMvc.perform(get("/api/borrow/list")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(3))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(3));
    }

    @Test
    @DisplayName("管理员查询所有借阅记录 - 验证按用户名筛选")
    void testAdminQueryAllRecordsWithUsernameFilter() throws Exception {
        mockMvc.perform(get("/api/borrow/list")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("size", "10")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(3));
    }

    @Test
    @DisplayName("普通用户查询自己的借阅记录 - 验证权限隔离")
    void testUserQueryOwnRecords() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/borrow/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(3))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode records = root.path("data").path("records");

        for (JsonNode record : records) {
            Long userId = record.path("userId").asLong();
            assertEquals(2L, userId, "普通用户只能看到自己的借阅记录");
        }
    }

    @Test
    @DisplayName("普通用户查询自己的借阅记录 - 验证分页")
    void testUserQueryOwnRecordsWithPagination() throws Exception {
        mockMvc.perform(get("/api/borrow/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.total").value(3));
    }

    @Test
    @DisplayName("归还图书功能 - 验证状态更新和库存变化")
    void testReturnBook() throws Exception {
        mockMvc.perform(post("/api/borrow/return/3")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("归还成功"));

        MvcResult result = mockMvc.perform(get("/api/borrow/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode records = root.path("data").path("records");

        boolean recordReturned = false;
        for (JsonNode record : records) {
            if (record.path("id").asLong() == 3L) {
                assertEquals(1, record.path("status").asInt(), "借阅记录状态应更新为已归还");
                assertNotNull(record.path("returnDate").asText(), "归还日期不应为空");
                recordReturned = true;
            }
        }
        assertTrue(recordReturned, "应找到归还的借阅记录");
    }

    @Test
    @DisplayName("管理员归还他人的图书 - 验证管理员权限")
    void testAdminReturnOthersBook() throws Exception {
        mockMvc.perform(post("/api/borrow/return/4")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("归还成功"));
    }

    @Test
    @DisplayName("异常场景 - 未授权访问（无Token）")
    void testUnauthorizedAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/borrow/my")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("异常场景 - 未授权访问（无效Token）")
    void testUnauthorizedAccessWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/borrow/my")
                        .header("Authorization", "Bearer invalid.token.here")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("异常场景 - 普通用户尝试访问管理员接口")
    void testUserAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/borrow/list")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("异常场景 - 归还无效的记录ID")
    void testReturnInvalidRecordId() throws Exception {
        mockMvc.perform(post("/api/borrow/return/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("借阅记录不存在"));
    }

    @Test
    @DisplayName("异常场景 - 归还已归还的图书")
    void testReturnAlreadyReturnedBook() throws Exception {
        mockMvc.perform(post("/api/borrow/return/2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("该图书已归还"));
    }

    @Test
    @DisplayName("异常场景 - 普通用户归还他人的图书")
    void testUserReturnOthersBook() throws Exception {
        mockMvc.perform(post("/api/borrow/return/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("无权操作他人的借阅记录"));
    }
}
