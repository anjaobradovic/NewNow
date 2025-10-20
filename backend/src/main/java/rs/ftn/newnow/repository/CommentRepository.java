package rs.ftn.newnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    @Query("SELECT c FROM Comment c WHERE c.review.id = :reviewId " +
           "AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findByReviewIdAndNotDeleted(@Param("reviewId") Long reviewId);
    
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId " +
           "AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findByParentCommentIdAndNotDeleted(@Param("parentCommentId") Long parentCommentId);
    
    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.deleted = false")
    Optional<Comment> findByIdAndNotDeleted(@Param("id") Long id);
}

