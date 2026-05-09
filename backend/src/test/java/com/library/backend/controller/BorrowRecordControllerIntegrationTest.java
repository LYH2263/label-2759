package com.library.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.backend.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:test-data/borrow-record-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
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
        adminToken = "Bearer " + jwtUtils.generateToken("admin", 1L);
        userToken = "Bearer " + jwtUtils.generateToken("testuser", 2L);
    }

    @Nested
    @DisplayName("管理员查询所有借阅记录 - GET /api/borrow/list")
    class AdminGetAllBorrowRecords {

        @Test
        @DisplayName("管理员可以查询所有借阅记录（分页）")
        void adminCanListAllBorrowRecordsWithPagination() throws Exception {
            mockMvc.perform(get("/api/borrow/list")
                            .param("page", "1")
                            .param("size", "10")
                            .header("Authorization", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records.length()").value(5))
                    .andExpect(jsonPath("$.data.total").value(5))
                    .andExpect(jsonPath("$.data.current").value(1));
        }

        @Test
        @DisplayName("管理员查询借阅记录 - 分页参数生效")
        void adminPaginationWorks() throws Exception {
            mockMvc.perform(get("/api/borrow/list")
                            .param("page", "1")
                            .param("size", "2")
                            .header("Authorization", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(2))
                    .andExpect(jsonPath("$.data.total").value(5))
                    .andExpect(jsonPath("$.data.pages").value(3));
        }

        @Test
        @DisplayName("管理员按用户名筛选借阅记录")
        void adminCanFilterByUsername() throws Exception {
            mockMvc.perform(get("/api/borrow/list")
                            .param("page", "1")
                            .param("size", "10")
                            .param("username", "testuser")
                            .header("Authorization", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records.length()").value(3));
        }

        @Test
        @DisplayName("管理员按用户名模糊筛选借阅记录")
        void adminCanFilterByUsernameFuzzy() throws Exception {
            mockMvc.perform(get("/api/borrow/list")
                            .param("page", "1")
                            .param("size", "10")
                            .param("username", "admin")
                            .header("Authorization", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(2));
        }

        @Test
        @DisplayName("普通用户无法访问管理员查询接口")
        void normalUserCannotAccessAdminListEndpoint() throws Exception {
            mockMvc.perform(get("/api/borrow/list")
                            .param("page", "1")
                            .param("size", "10")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(403));
        }
    }

    @Nested
    @DisplayName("普通用户查询自己的借阅记录 - GET /api/borrow/my")
    class UserGetMyBorrowRecords {

        @Test
        @DisplayName("用户可以查询自己的借阅记录")
        void userCanGetOwnBorrowRecords() throws Exception {
            mockMvc.perform(get("/api/borrow/my")
                            .param("page", "1")
                            .param("size", "10")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records.length()").value(3))
                    .andExpect(jsonPath("$.data.total").value(3));
        }

        @Test
        @DisplayName("用户查询借阅记录 - 权限隔离，只能看到自己的记录")
        void userCanOnlySeeOwnRecords() throws Exception {
            ResultActions result = mockMvc.perform(get("/api/borrow/my")
                            .param("page", "1")
                            .param("size", "10")
                            .header("Authorization", userToken));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(3));

            for (int i = 0; i < 3; i++) {
                result.andExpect(jsonPath("$.data.records[" + i + "].userId").value(2));
            }
        }

        @Test
        @DisplayName("管理员查询自己的借阅记录只能看到自己的")
        void adminCanOnlySeeOwnRecordsInMyEndpoint() throws Exception {
            mockMvc.perform(get("/api/borrow/my")
                            .param("page", "1")
                            .param("size", "10")
                            .header("Authorization", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(2));
        }

        @Test
        @DisplayName("用户查询借阅记录 - 分页参数生效")
        void userPaginationWorks() throws Exception {
            mockMvc.perform(get("/api/borrow/my")
                            .param("page", "1")
                            .param("size", "2")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(2))
                    .andExpect(jsonPath("$.data.total").value(3))
                    .andExpect(jsonPath("$.data.pages").value(2));
        }
    }

    @Nested
    @DisplayName("归还图书功能 - POST /api/borrow/return/{id}")
    class ReturnBook {

        @Test
        @DisplayName("用户可以归还自己借阅的图书")
        void userCanReturnOwnBorrowedBook() throws Exception {
            mockMvc.perform(post("/api/borrow/return/1")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("归还成功"));
        }

        @Test
        @DisplayName("归还图书后状态更新为已归还(status=1)")
        void returnBookUpdatesStatus() throws Exception {
            mockMvc.perform(post("/api/borrow/return/1")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            mockMvc.perform(get("/api/borrow/my")
                            .param("page", "1")
                            .param("size", "10")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("归还图书后库存增加")
        void returnBookIncreasesStock() throws Exception {
            mockMvc.perform(post("/api/borrow/return/1")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("不能重复归还已归还的图书")
        void cannotReturnAlreadyReturnedBook() throws Exception {
            mockMvc.perform(post("/api/borrow/return/2")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("该图书已归还"));
        }

        @Test
        @DisplayName("管理员可以归还他人的借阅记录")
        void adminCanReturnOtherUsersRecord() throws Exception {
            mockMvc.perform(post("/api/borrow/return/1")
                            .header("Authorization", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("归还成功"));
        }
    }

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionScenarios {

        @Test
        @DisplayName("未授权访问 - 无Token")
        void accessWithoutTokenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/borrow/my")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("未授权访问 - 无效Token")
        void accessWithInvalidTokenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/borrow/my")
                            .param("page", "1")
                            .param("size", "10")
                            .header("Authorization", "Bearer invalidtoken123"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("归还不存在的借阅记录")
        void returnNonExistentRecordReturnsError() throws Exception {
            mockMvc.perform(post("/api/borrow/return/9999")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("借阅记录不存在"));
        }

        @Test
        @DisplayName("普通用户无法归还他人的借阅记录")
        void normalUserCannotReturnOtherUsersRecord() throws Exception {
            mockMvc.perform(post("/api/borrow/return/3")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("无权操作他人的借阅记录"));
        }

        @Test
        @DisplayName("管理员列表接口 - 未授权访问")
        void adminListWithoutTokenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/borrow/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
