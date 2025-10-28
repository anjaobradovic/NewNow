import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CommentDTO,
  CreateCommentRequest,
  CreateReviewRequest,
  PageResponse,
  ReviewDTO,
  ReviewDetailsDTO,
  UpdateReviewRequest,
} from '../models/user.model';
import { MessageResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly API_URL = '/api';

  constructor(private http: HttpClient) {}

  // Create new review for a location
  createReview(locationId: number, dto: CreateReviewRequest): Observable<ReviewDetailsDTO> {
    return this.http.post<ReviewDetailsDTO>(`${this.API_URL}/locations/${locationId}/reviews`, dto);
  }

  // Review details
  getReview(id: number): Observable<ReviewDetailsDTO> {
    return this.http.get<ReviewDetailsDTO>(`${this.API_URL}/reviews/${id}`);
  }

  // Update/delete own review
  updateReview(id: number, dto: UpdateReviewRequest): Observable<ReviewDetailsDTO> {
    return this.http.put<ReviewDetailsDTO>(`${this.API_URL}/reviews/${id}`, dto);
  }
  deleteReview(id: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.API_URL}/reviews/${id}`);
  }

  // Comments thread
  getComments(reviewId: number): Observable<CommentDTO[]> {
    return this.http.get<CommentDTO[]>(`${this.API_URL}/reviews/${reviewId}/comments`);
  }
  addComment(reviewId: number, dto: CreateCommentRequest): Observable<CommentDTO> {
    return this.http.post<CommentDTO>(`${this.API_URL}/reviews/${reviewId}/comments`, dto);
  }
  deleteComment(reviewId: number, commentId: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(
      `${this.API_URL}/reviews/${reviewId}/comments/${commentId}`
    );
  }

  // Manager moderation
  hideReview(id: number, hidden: boolean): Observable<MessageResponse> {
    return this.http.patch<MessageResponse>(`${this.API_URL}/manager/reviews/${id}/hide`, {
      hidden,
    });
  }
  deleteByManager(id: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.API_URL}/manager/reviews/${id}`);
  }

  // Manager view - includes hidden reviews
  getLocationReviewsForManager(
    locationId: number,
    sort: 'rating' | 'date',
    order: 'asc' | 'desc',
    page = 0,
    size = 10
  ): Observable<PageResponse<ReviewDetailsDTO>> {
    const params = new HttpParams()
      .set('sort', sort)
      .set('order', order)
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<ReviewDetailsDTO>>(
      `${this.API_URL}/manager/locations/${locationId}/reviews`,
      { params }
    );
  }

  // For location details page sorted feed (uses ReviewDetailsDTO)
  getLocationReviewsSorted(
    locationId: number,
    sort: 'rating' | 'date',
    order: 'asc' | 'desc',
    page = 0,
    size = 10
  ): Observable<PageResponse<ReviewDetailsDTO>> {
    const params = new HttpParams()
      .set('sort', sort)
      .set('order', order)
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<ReviewDetailsDTO>>(
      `${this.API_URL}/locations/${locationId}/reviews/sort`,
      { params }
    );
  }
}
