import { Component, OnInit, signal, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../services/auth.service';
import { CommentDTO, CreateCommentRequest, ReviewDetailsDTO } from '../../models/user.model';

@Component({
  selector: 'app-comment-item',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="border border-neutral-100 rounded-xl p-4">
      <div class="flex justify-between items-start">
        <div>
          <div class="text-sm font-medium">{{ comment.author.name }}</div>
          <div class="text-xs text-neutral-500">{{ comment.createdAt | date : 'medium' }}</div>
        </div>
        <div class="flex items-center gap-2">
          <button class="text-primary-700 text-sm" (click)="toggleReply()">Reply</button>
          <button class="text-neutral-500 text-sm" (click)="onDelete.emit(comment.id)">
            Delete
          </button>
        </div>
      </div>
      <p class="mt-2 text-neutral-700">{{ comment.text }}</p>

      <div *ngIf="replyOpen" class="mt-3">
        <textarea
          class="input-field w-full"
          [(ngModel)]="replyText"
          placeholder="Reply..."
        ></textarea>
        <div class="mt-2 flex justify-end">
          <button class="btn-primary" (click)="submitReply()">Send</button>
        </div>
      </div>

      <div class="mt-4 space-y-3 ml-4">
        <app-comment-item
          *ngFor="let r of comment.replies"
          [comment]="r"
          (onReply)="onReply.emit($event)"
          (onDelete)="onDelete.emit($event)"
        ></app-comment-item>
      </div>
    </div>
  `,
})
export class CommentItemComponent {
  @Input() comment!: CommentDTO;
  @Output() onReply = new EventEmitter<{ parentId: number; text: string }>();
  @Output() onDelete = new EventEmitter<number>();

  replyOpen = false;
  replyText = '';

  toggleReply() {
    this.replyOpen = !this.replyOpen;
  }

  submitReply() {
    const text = this.replyText.trim();
    if (!text) return;
    this.onReply.emit({ parentId: this.comment.id, text });
    this.replyText = '';
    this.replyOpen = false;
  }
}

@Component({
  selector: 'app-review-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, CommentItemComponent],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-4xl mx-auto px-4 space-y-6">
        <a routerLink="/locations" class="text-primary-700 hover:underline">&larr; Back</a>

        <div *ngIf="loading()" class="card p-6 animate-pulse">
          <div class="h-6 bg-neutral-200 w-1/3 rounded"></div>
          <div class="h-4 bg-neutral-200 w-1/2 rounded mt-3"></div>
        </div>

        <div *ngIf="!loading() && review() as r" class="space-y-6">
          <div class="card p-6">
            <div class="flex items-start justify-between">
              <div>
                <h1 class="text-2xl font-bold text-neutral-900">Review for {{ r.event.name }}</h1>
                <p class="text-neutral-500 text-sm">
                  {{ r.createdAt | date : 'medium' }} • #{{ r.eventCount }}
                </p>
              </div>
              <div class="flex items-center gap-3">
                <span class="px-2 py-1 rounded-lg bg-primary-100 text-primary-700 text-sm"
                  >Avg {{ r.ratings.average | number : '1.1-1' }}</span
                >
                <ng-container *ngIf="canEdit(r)">
                  <a class="btn-secondary" [routerLink]="['/reviews', r.id, 'edit']">Edit</a>
                  <button class="btn-primary" (click)="confirmDelete = true">Delete</button>
                </ng-container>
              </div>
            </div>

            <div class="mt-4 grid grid-cols-2 md:grid-cols-4 gap-3">
              <div class="card p-3 text-center" *ngIf="r.ratings.performance">
                <div class="text-xs text-neutral-500">Performance</div>
                <div class="text-xl font-semibold">{{ r.ratings.performance }}</div>
              </div>
              <div class="card p-3 text-center" *ngIf="!r.ratings.performance">
                <div class="text-xs text-neutral-500">Performance</div>
                <div class="text-xl font-semibold text-neutral-400">N/A</div>
              </div>

              <div class="card p-3 text-center" *ngIf="r.ratings.soundAndLighting">
                <div class="text-xs text-neutral-500">Sound & Lighting</div>
                <div class="text-xl font-semibold">{{ r.ratings.soundAndLighting }}</div>
              </div>
              <div class="card p-3 text-center" *ngIf="!r.ratings.soundAndLighting">
                <div class="text-xs text-neutral-500">Sound & Lighting</div>
                <div class="text-xl font-semibold text-neutral-400">N/A</div>
              </div>

              <div class="card p-3 text-center" *ngIf="r.ratings.venue">
                <div class="text-xs text-neutral-500">Venue</div>
                <div class="text-xl font-semibold">{{ r.ratings.venue }}</div>
              </div>
              <div class="card p-3 text-center" *ngIf="!r.ratings.venue">
                <div class="text-xs text-neutral-500">Venue</div>
                <div class="text-xl font-semibold text-neutral-400">N/A</div>
              </div>

              <div class="card p-3 text-center" *ngIf="r.ratings.overallImpression">
                <div class="text-xs text-neutral-500">Overall</div>
                <div class="text-xl font-semibold">{{ r.ratings.overallImpression }}</div>
              </div>
              <div class="card p-3 text-center" *ngIf="!r.ratings.overallImpression">
                <div class="text-xs text-neutral-500">Overall</div>
                <div class="text-xl font-semibold text-neutral-400">N/A</div>
              </div>
            </div>

            <p class="mt-4 text-neutral-700" *ngIf="r.comment">"{{ r.comment }}"</p>
            <div class="mt-2 text-sm text-neutral-500">
              by {{ r.author.name }} ({{ r.author.email }})
            </div>
          </div>

          <!-- Comments thread -->
          <div class="card p-6">
            <h2 class="text-xl font-semibold mb-4">Discussion</h2>

            <div *ngIf="auth.isAuthenticated()" class="mb-6">
              <textarea
                class="input-field w-full"
                [(ngModel)]="newComment"
                placeholder="Add a comment"
              ></textarea>
              <div class="mt-2 flex justify-end">
                <button class="btn-primary" (click)="postComment()">Comment</button>
              </div>
            </div>

            <div *ngFor="let c of comments()" class="mb-5">
              <app-comment-item
                [comment]="c"
                (onReply)="onReply($event)"
                (onDelete)="onDelete($event)"
              ></app-comment-item>
            </div>
          </div>
        </div>

        <!-- Delete modal -->
        <div
          *ngIf="confirmDelete"
          class="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center p-4"
        >
          <div class="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
            <h3 class="text-lg font-semibold">Delete review?</h3>
            <p class="text-neutral-600 mt-1">This action cannot be undone.</p>
            <div class="mt-6 flex justify-end gap-3">
              <button class="btn-secondary" (click)="confirmDelete = false">Cancel</button>
              <button class="btn-primary" (click)="deleteReview()">Delete</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class ReviewDetailsComponent implements OnInit {
  id!: number;
  loading = signal(true);
  review = signal<ReviewDetailsDTO | null>(null);
  comments = signal<CommentDTO[]>([]);
  newComment = '';
  confirmDelete = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reviewService: ReviewService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.reviewService.getReview(this.id).subscribe((r) => {
      this.review.set(r);
      this.loading.set(false);
    });
    this.reviewService.getComments(this.id).subscribe((list) => this.comments.set(list));
  }

  canEdit(r: ReviewDetailsDTO | null): boolean {
    const email = this.auth.currentUser()?.email;
    return !!r && r.author.email === email;
  }

  postComment() {
    if (!this.newComment.trim()) return;
    const dto: CreateCommentRequest = { text: this.newComment };
    this.reviewService.addComment(this.id, dto).subscribe(() => {
      this.newComment = '';
      this.refresh();
    });
  }

  onReply(payload: { parentId: number; text: string }) {
    const dto: CreateCommentRequest = { text: payload.text, parentCommentId: payload.parentId };
    this.reviewService.addComment(this.id, dto).subscribe(() => this.refresh());
  }

  onDelete(commentId: number) {
    this.reviewService.deleteComment(this.id, commentId).subscribe(() => this.refresh());
  }

  deleteReview() {
    this.reviewService.deleteReview(this.id).subscribe(() => this.router.navigate(['/me/reviews']));
  }
}
