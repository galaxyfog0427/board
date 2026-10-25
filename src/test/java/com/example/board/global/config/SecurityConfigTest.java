package com.example.board.global.config;

import com.example.board.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("로그인하지 않아도 에러 페이지는 로그인 화면으로 리다이렉트되지 않는다")
    void errorPageIsAccessibleWithoutLogin() throws Exception {
        mockMvc.perform(get("/error").accept(MediaType.TEXT_HTML))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("일시적인 오류가 발생했습니다")));
    }

}
