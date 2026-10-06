package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void homePageLoadsWithExamCatalog() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("home"))
				.andExpect(model().attributeExists("exams"));
	}

	@Test
	void registeredUserCanOpenAndSubmitExam() throws Exception {
		mockMvc.perform(post("/signup")
					.param("name", "Test Student")
					.param("email", "student@example.com")
					.param("username", "teststudent")
					.param("password", "secret")
					.param("confirmPassword", "secret"))
				.andExpect(view().name("signup"))
				.andExpect(model().attributeExists("message"));

		MvcResult loginResult = mockMvc.perform(post("/login")
					.param("username", "teststudent")
					.param("password", "secret"))
				.andExpect(status().is3xxRedirection())
				.andReturn();
		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(get("/exam/algebra").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("exam"));

		mockMvc.perform(post("/exam/algebra").session(session)
					.param("question0", "1")
					.param("question1", "2")
					.param("question2", "2"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("score", 3))
				.andExpect(model().attribute("total", 3));
	}

	@Test
	void unauthenticatedUserIsSentToLoginForExam() throws Exception {
		mockMvc.perform(get("/exam/algebra"))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/login"));
	}

	@Test
	void logoutReturnsUserToHome() throws Exception {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("username", "teststudent");

		mockMvc.perform(get("/logout").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/"));
	}

}
