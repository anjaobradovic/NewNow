package rs.ftn.newnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByReviewId(Long reviewId);
    
    List<Comment> findByParentCommentId(Long parentCommentId);
}
