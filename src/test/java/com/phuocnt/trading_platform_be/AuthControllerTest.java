package com.phuocnt.trading_platform_be;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.phuocnt.trading_platform_be.dto.request.RegisterRequest;
import com.phuocnt.trading_platform_be.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// Dùng Jackson mới của Spring Boot 4.x
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private org.springframework.web.context.WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Bỏ @AutoConfigureMockMvc, setup MockMvc thủ công
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Xoá user test cũ
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    // Lưu token dùng giữa các test
    static String accessToken;

    // Email cố định dùng xuyên suốt test
    static final String TEST_EMAIL = "junit_test@gmail.com";
    static final String TEST_PASSWORD = "123456";

    @BeforeEach
    void cleanUp() {
        // Xoá user test trước mỗi lần chạy để tránh conflict
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
    }

    // ===================== SIGNUP =====================

    @Test
    @Order(1)
    @DisplayName("Signup thành công → 201 + accessToken")
    void signup_success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("JUnit Test User");
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Signup successful"))
                .andReturn();

        // Lưu token cho các test sau
        String body = result.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(body).get("accessToken").asText();
    }

    @Test
    @Order(2)
    @DisplayName("Signup email trùng → 409 Conflict")
    void signup_duplicateEmail() throws Exception {
        // Tạo user trước
        RegisterRequest req = new RegisterRequest();
        req.setFullName("JUnit Test User");
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // Signup lần 2 cùng email → phải 409
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("Signup thiếu email → không tạo được user")
    void signup_missingEmail() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("No Email User");
        req.setPassword(TEST_PASSWORD);
        // email null

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    // ===================== SIGNIN =====================

    @Test
    @Order(4)
    @DisplayName("Signin thành công → 200 + accessToken")
    void signin_success() throws Exception {
        // Tạo user trước
        RegisterRequest signup = new RegisterRequest();
        signup.setFullName("JUnit Test User");
        signup.setEmail(TEST_EMAIL);
        signup.setPassword(TEST_PASSWORD);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));

        // Login
        Map<String, String> loginReq = Map.of(
                "email", TEST_EMAIL,
                "password", TEST_PASSWORD
        );

        MvcResult result = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.status").value(true))
                .andReturn();

        accessToken = objectMapper.readTree(
                result.getResponse().getContentAsString()
        ).get("accessToken").asText();
    }

    @Test
    @Order(5)
    @DisplayName("Signin sai password → 400")
    void signin_wrongPassword() throws Exception {
        // Tạo user trước
        RegisterRequest signup = new RegisterRequest();
        signup.setFullName("JUnit Test User");
        signup.setEmail(TEST_EMAIL);
        signup.setPassword(TEST_PASSWORD);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));

        Map<String, String> loginReq = Map.of(
                "email", TEST_EMAIL,
                "password", "sat_mat_khau"
        );

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(containsString("incorrect")));
    }

    @Test
    @Order(6)
    @DisplayName("Signin email không tồn tại → 400")
    void signin_emailNotFound() throws Exception {
        Map<String, String> loginReq = Map.of(
                "email", "khongtontai@gmail.com",
                "password", TEST_PASSWORD
        );

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));
    }

    // ===================== USER PROFILE =====================

    @Test
    @Order(7)
    @DisplayName("Get profile có token → 200 + user info")
    void getProfile_withToken() throws Exception {
        // Signup + lấy token
        RegisterRequest signup = new RegisterRequest();
        signup.setFullName("JUnit Test User");
        signup.setEmail(TEST_EMAIL);
        signup.setPassword(TEST_PASSWORD);

        MvcResult signupResult = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andReturn();

        String token = objectMapper.readTree(
                signupResult.getResponse().getContentAsString()
        ).get("accessToken").asText();

        // Get profile
        mockMvc.perform(get("/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist()); // password không được lộ
    }

    @Test
    @Order(8)
    @DisplayName("Get profile không có token → 401")
    void getProfile_noToken() throws Exception {
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(9)
    @DisplayName("Get profile token giả → 401")
    void getProfile_fakeToken() throws Exception {
        mockMvc.perform(get("/user/profile")
                        .header("Authorization", "Bearer token_gia_mao_xyz"))
                .andExpect(status().isUnauthorized());
    }

    // ===================== FORGOT PASSWORD =====================

    @Test
    @Order(10)
    @DisplayName("Reset password - gửi OTP email không tồn tại → 400")
    void resetPassword_emailNotFound() throws Exception {
        Map<String, String> req = Map.of(
                "sendTo", "khongtontai@gmail.com",
                "verificationType", "EMAIL"
        );

        mockMvc.perform(post("/auth/reset-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    @DisplayName("Reset password - verify OTP sai → 400")
    void resetPassword_wrongOtp() throws Exception {
        // Tạo user trước
        RegisterRequest signup = new RegisterRequest();
        signup.setFullName("JUnit Test User");
        signup.setEmail(TEST_EMAIL);
        signup.setPassword(TEST_PASSWORD);
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));

        // Gửi OTP
        Map<String, String> sendOtpReq = Map.of(
                "sendTo", TEST_EMAIL,
                "verificationType", "EMAIL"
        );

        MvcResult sendResult = mockMvc.perform(post("/auth/reset-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendOtpReq)))
                .andReturn();

        String session = objectMapper.readTree(
                sendResult.getResponse().getContentAsString()
        ).get("session").asText();

        // Verify OTP sai
        Map<String, String> verifyReq = Map.of(
                "otp", "000000",
                "password", "newpassword123"
        );

        mockMvc.perform(post("/auth/reset-password/verify-otp")
                        .param("id", session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Wrong")));
    }

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "test"))
            .withPerMethodLifecycle(false);
}
