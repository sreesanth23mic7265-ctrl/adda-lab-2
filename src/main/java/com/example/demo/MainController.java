package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class MainController {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Exam> exams = createExams();

    @GetMapping("/")
    public String getMethodName(Model model, HttpSession session) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("exams", exams.values());
        return "home";
    }
    @PostMapping("/path")
    @ResponseBody
    public String postMethodName(@RequestBody String entity) {
        return entity;
    }
    

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        User registeredUser = users.get(username);
        if (registeredUser == null || !registeredUser.password().equals(password)) {
            model.addAttribute("error", "Username or password is incorrect.");
            return "login";
        }

        session.setAttribute("username", username);
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "signup";
        }

        if (users.containsKey(username)) {
            model.addAttribute("error", "Username is already taken. Please choose a different one.");
            return "signup";
        }

        users.put(username, new User(name, email, username, password));
        model.addAttribute("message", "Account created for " + name + ". You can now log in.");
        return "signup";
    }

    @GetMapping("/exam/{id}")
    public String showExam(@PathVariable String id, Model model, HttpSession session) {
        Exam exam = exams.get(id);
        if (exam == null || session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        model.addAttribute("exam", exam);
        return "exam";
    }

    @PostMapping("/exam/{id}")
    public String submitExam(@PathVariable String id,
                             @RequestParam Map<String, String> answers,
                             Model model,
                             HttpSession session) {
        Exam exam = exams.get(id);
        if (exam == null || session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        int score = 0;
        for (int questionIndex = 0; questionIndex < exam.questions().size(); questionIndex++) {
            String submittedAnswer = answers.get("question" + questionIndex);
            if (submittedAnswer != null && Integer.parseInt(submittedAnswer) == exam.questions().get(questionIndex).correctAnswer()) {
                score++;
            }
        }

        model.addAttribute("exam", exam);
        model.addAttribute("score", score);
        model.addAttribute("total", exam.questions().size());
        model.addAttribute("submitted", true);
        return "exam";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private static Map<String, Exam> createExams() {
        Map<String, Exam> catalog = new LinkedHashMap<>();
        catalog.put("algebra", new Exam("algebra", "Algebra fundamentals", "MATH", "20 questions", "30 minutes", List.of(
                new Question("What is the value of 3x when x = 4?", List.of("7", "12", "16", "1"), 1),
                new Question("Which expression is equivalent to 2(x + 3)?", List.of("2x + 3", "x + 6", "2x + 6", "2x + 9"), 2),
                new Question("What is the slope of y = 5x - 2?", List.of("-2", "2", "5", "-5"), 2))));
        catalog.put("biology", new Exam("biology", "Cell biology checkpoint", "BIO", "15 questions", "20 minutes", List.of(
                new Question("Which structure controls activities in a cell?", List.of("Cell wall", "Nucleus", "Ribosome", "Cytoplasm"), 1),
                new Question("What process do plants use to make glucose?", List.of("Respiration", "Digestion", "Photosynthesis", "Diffusion"), 2),
                new Question("Which organelle produces most cellular energy?", List.of("Mitochondria", "Vacuole", "Golgi body", "Lysosome"), 0))));
        catalog.put("reading", new Exam("reading", "Reading comprehension", "ENG", "25 questions", "35 minutes", List.of(
                new Question("What is the main purpose of a topic sentence?", List.of("End an essay", "Introduce a paragraph's main idea", "List sources", "Add a quotation"), 1),
                new Question("A comparison looks for what between two ideas?", List.of("Similarities", "Dates", "Definitions", "Opposites only"), 0),
                new Question("Which source is usually strongest for a research claim?", List.of("Anonymous comment", "Peer-reviewed study", "Advertisement", "Personal guess"), 1))));
        return catalog;
    }

    private record User(String name, String email, String username, String password) {
    }

    private record Exam(String id, String title, String code, String questionCount, String duration, List<Question> questions) {
    }

    private record Question(String prompt, List<String> options, int correctAnswer) {
    }
}
