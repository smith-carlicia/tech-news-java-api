package com.technews.controller;

import com.technews.mode.Comment;
import com.technews.mode.Post;
import com.technews.mode.User;
import com.technews.mode.Vote;
import com.technews.repository.CommentRepository;
import com.technews.repository.PostRepository;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

@Controller
public class TechNewsController {

    @Autowired
    PostRepository postRepository;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

//    post login endpoint
    @PostMapping("/users/login")
    public String login(@ModelAttribute User user, Model model, HttpServletRequestWrapper request) throws Exception {

        if ((user.getPassword().equals(null) || user.getPassword().isEmpty()) || (user.getEmail().equals(null) || user.getPassword().isEmpty())) {
            model.addAttribute("notice", "Email address and password must be populated in order to login!");
            return "login";
        }

        User sessionUser = userRepository.findUserByEmail(user.getEmail());

        try {
            if (sessionUser.equals(null)) {

            }
        } catch (NullPointerException e) {
            model.addAttribute("notice", "Email address is not recognized!");
            return "login";
        }

        // Validate Password
        String sessionUserPassword = sessionUser.getPassword();
        boolean isPasswordValid = BCrypt.checkpw(user.getPassword(), sessionUserPassword);
        if(isPasswordValid == false) {
            model.addAttribute("notice", "Password is not valid!");
            return "login";
        }

        sessionUser.setLoggedIn(true);
        request.getSession().setAttribute("SESSION_USER", sessionUser);

        return "redirect:/dashboard";
    }

//    get endpoint
    @PostMapping("/posts")
    public String addPostDashboardPage(@ModelAttribute Post post, Model model, HttpServletRequest request) {

        if ((post.getTitle().equals(null) || post.getTitle().isEmpty()) || (post.getPostUrl().equals(null) || post.getPostUrl().isEmpty())) {
            return "redirect:/dashboardEmptyTitleAndLink";
        }

        if (request.getSession(false) == null) {
            return "redirect:/login";
        } else {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            post.setUserId(sessionUser.getId());
            postRepository.save(post);

            return "redirect:/dashboard";
        }
    }

//    update posts
@PostMapping("/posts/{id}")
public String updatePostDashboardPage(@PathVariable int id, @ModelAttribute Post post, Model model, HttpServletRequest request) {

    if (request.getSession(false) == null) {
        model.addAttribute("user", new User());
        return "redirect/dashboard";
    } else {
        Post tempPost = postRepository.getOne(id);
        tempPost.setTitle(post.getTitle());
        postRepository.save(tempPost);

        return "redirect:/dashboard";
    }
}

//    users can comment on their posts or others posts
@PostMapping("/comments")
public String createCommentCommentsPage(@ModelAttribute Comment comment, Model model, HttpServletRequest request) {

    if (comment.getCommentText().isEmpty() || comment.getCommentText().equals(null)) {
        return "redirect:/singlePostEmptyComment/" + comment.getPostId();
    } else {
        if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            comment.setUserId(sessionUser.getId());
            commentRepository.save(comment);
            return "redirect:/post/" + comment.getPostId();
        } else {
            return "login";
        }
    }
}

//    edit comments
@PostMapping("/comments/edit")
public String createCommentEditPage(@ModelAttribute Comment comment, HttpServletRequest request) {

    if (comment.getCommentText().equals("") || comment.getCommentText().equals(null)) {
        return "redirect:/editPostEmptyComment/" + comment.getPostId();
    } else {
        if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            comment.setUserId(sessionUser.getId());
            commentRepository.save(comment);

            return "redirect:/dashboard/edit/" + comment.getPostId();
        } else {
            return "redirect:/login";
        }
    }

}

//upvote posts
@PutMapping("/posts/upvote")
public void addVoteCommentsPage(@RequestBody Vote vote, HttpServletRequest request, HttpServletResponse response) {

    if (request.getSession(false) != null) {
        Post returnPost = null;
        User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
        vote.setUserId(sessionUser.getId());
        voteRepository.save(vote);

        returnPost = postRepository.getOne(vote.getPostId());
        returnPost.setVoteCount(voteRepository.countVotesByPostId(vote.getPostId()));
    }
}
}
