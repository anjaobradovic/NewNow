package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.CommentDTO;
import rs.ftn.newnow.dto.CreateCommentDTO;
import rs.ftn.newnow.dto.UserBasicDTO;
import rs.ftn.newnow.model.Comment;
import rs.ftn.newnow.model.Review;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.repository.CommentRepository;
import rs.ftn.newnow.repository.ManagesRepository;
import rs.ftn.newnow.repository.ReviewRepository;
import rs.ftn.newnow.repository.UserRepository;
import rs.ftn.newnow.exception.BusinessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ManagesRepository managesRepository;

    @Transactional
    public CommentDTO createComment(Long reviewId, CreateCommentDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));

        Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setUser(user);
        comment.setReview(review);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setDeleted(false);

        if (dto.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findByIdAndNotDeleted(dto.getParentCommentId())
                    .orElseThrow(() -> new BusinessException("Parent comment not found"));
            
            if (!parentComment.getReview().getId().equals(reviewId)) {
                throw new BusinessException("Parent comment does not belong to this review");
            }
            
            comment.setParentComment(parentComment);
        }

        comment = commentRepository.save(comment);
        return mapToDTO(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentTree(Long reviewId) {
        reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        List<Comment> rootComments = commentRepository.findByReviewIdAndNotDeleted(reviewId)
                .stream()
                .filter(c -> c.getParentComment() == null)
                .collect(Collectors.toList());

        return rootComments.stream()
                .map(this::mapToTreeDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long reviewId, Long commentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));

        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new BusinessException("Comment not found"));

        if (!comment.getReview().getId().equals(reviewId)) {
            throw new BusinessException("Comment does not belong to this review");
        }

        boolean isAuthor = comment.getUser().getId().equals(user.getId());
        
        boolean isManager = !managesRepository.findActiveManagement(
                user.getId(), 
                comment.getReview().getLocation().getId(), 
                LocalDate.now()
        ).isEmpty();

        if (!isAuthor && !isManager) {
            throw new BusinessException("You can only delete your own comments or comments on your managed location");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    private CommentDTO mapToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null);
        dto.setReplies(new ArrayList<>());

        UserBasicDTO authorDTO = new UserBasicDTO();
        authorDTO.setId(comment.getUser().getId());
        authorDTO.setName(comment.getUser().getName());
        authorDTO.setEmail(comment.getUser().getEmail());
        dto.setAuthor(authorDTO);

        return dto;
    }

    private CommentDTO mapToTreeDTO(Comment comment) {
        CommentDTO dto = mapToDTO(comment);
        
        List<Comment> replies = commentRepository.findByParentCommentIdAndNotDeleted(comment.getId());
        dto.setReplies(replies.stream()
                .map(this::mapToTreeDTO)
                .collect(Collectors.toList()));

        return dto;
    }
}
