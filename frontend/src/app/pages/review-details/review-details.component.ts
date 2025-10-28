import { Component, OnInit, signal, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { CommentDTO, CreateCommentRequest, ReviewDetailsDTO } from '../../models/user.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-comment-item',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div
      class="border-l-4 rounded-lg p-4 mb-3 transition-all hover:shadow-md"
      [class.border-blue-500]="comment.author.role === 'ROLE_MANAGER'"
      [class.bg-blue-50]="comment.author.role === 'ROLE_MANAGER'"
      [class.border-neutral-300]="comment.author.role !== 'ROLE_MANAGER'"
      [class.bg-white]="comment.author.role !== 'ROLE_MANAGER'"
    >
      <div class="flex justify-between items-start">
        <div class="flex items-start gap-3">
          <div
            class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-white"
            [class.bg-gradient-to-br]="true"
            [class.from-blue-500]="comment.author.role === 'ROLE_MANAGER'"
            [class.to-purple-500]="comment.author.role === 'ROLE_MANAGER'"
            [class.from-neutral-400]="comment.author.role !== 'ROLE_MANAGER'"
            [class.to-neutral-500]="comment.author.role !== 'ROLE_MANAGER'"
          >
            {{ comment.author.name.charAt(0).toUpperCase() }}
          </div>
          <div>
            <div class="flex items-center gap-2">
              <span class="text-sm font-semibold text-neutral-800">{{ comment.author.name }}</span>
              <span
                *ngIf="comment.author.role === 'ROLE_MANAGER'"
                class="px-2 py-0.5 bg-blue-600 text-white text-xs rounded-full font-medium"
              >
                <i class="fas fa-shield-alt mr-1"></i>Manager
              </span>
            </div>
            <div class="text-xs text-neutral-500">
              <i class="far fa-clock mr-1"></i>{{ comment.createdAt | date : 'medium' }}
            </div>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button
            class="text-blue-600 hover:text-blue-700 text-sm font-medium px-3 py-1 hover:bg-blue-100 rounded-lg transition-all"
            (click)="toggleReply()"
          >
            <i class="fas fa-reply mr-1"></i>Reply
          </button>
          <button
            class="text-red-600 hover:text-red-700 text-sm font-medium px-3 py-1 hover:bg-red-100 rounded-lg transition-all"
            (click)="onDelete.emit(comment.id)"
          >
            <i class="fas fa-trash-alt mr-1"></i>Delete
          </button>
        </div>
      </div>
      <p class="mt-3 text-neutral-700 leading-relaxed">{{ comment.text }}</p>

      <div *ngIf="replyOpen" class="mt-4 p-3 bg-white rounded-lg border border-neutral-300">
        <textarea
          class="w-full px-4 py-3 border-2 border-neutral-300 rounded-lg focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all resize-none"
          rows="3"
          [(ngModel)]="replyText"
          placeholder="Write your reply..."
        ></textarea>
        <div class="mt-2 flex justify-end gap-2">
          <button
            class="px-4 py-2 bg-neutral-200 text-neutral-700 rounded-lg hover:bg-neutral-300 transition-all"
            (click)="toggleReply()"
          >
            Cancel
          </button>
          <button
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all"
            (click)="submitReply()"
          >
            <i class="fas fa-paper-plane mr-2"></i>Send Reply
          </button>
        </div>
      </div>

      <div class="mt-4 space-y-3 ml-8 border-l-2 border-neutral-200 pl-4">
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
    <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 py-10">
      <div class="max-w-5xl mx-auto px-4 space-y-6">
        <a
          routerLink="/locations"
          class="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 font-medium transition-all"
        >
          <i class="fas fa-arrow-left"></i>Back to Locations
        </a>

        <div *ngIf="loading()" class="bg-white rounded-xl shadow-md p-8 animate-pulse">
          <div class="h-8 bg-neutral-200 w-1/3 rounded mb-4"></div>
          <div class="h-4 bg-neutral-200 w-1/2 rounded"></div>
        </div>

        <div *ngIf="!loading() && review() as r" class="space-y-6">
          <!-- Review Card -->
          <div class="bg-white rounded-xl shadow-lg border border-neutral-200 overflow-hidden">
            <!-- Header -->
            <div class="bg-gradient-to-r from-blue-500 to-purple-500 p-6 text-white">
              <div class="flex items-start justify-between">
                <div>
                  <h1 class="text-3xl font-bold mb-2">{{ r.event.name }}</h1>
                  <div class="flex items-center gap-4 text-sm opacity-90">
                    <span
                      ><i class="far fa-clock mr-2"></i>{{ r.createdAt | date : 'medium' }}</span
                    >
                    <span><i class="fas fa-hashtag mr-2"></i>Occurrence #{{ r.eventCount }}</span>
                  </div>
                  <span
                    *ngIf="r.hidden"
                    class="inline-block mt-3 px-3 py-1 rounded-full text-xs font-medium bg-yellow-500 text-white"
                  >
                    <i class="fas fa-eye-slash mr-1"></i>Hidden by manager
                  </span>
                </div>
                <div class="flex flex-col items-end gap-3">
                  <div class="px-6 py-3 bg-white bg-opacity-20 backdrop-blur-sm rounded-lg">
                    <div class="text-xs opacity-90 mb-1">Average Rating</div>
                    <div class="text-3xl font-bold">{{ r.ratings.average | number : '1.1-1' }}</div>
                  </div>
                  <div class="flex items-center gap-2" *ngIf="canEdit(r)">
                    <a
                      class="px-4 py-2 bg-white text-blue-600 rounded-lg hover:bg-opacity-90 transition-all font-medium"
                      [routerLink]="['/reviews', r.id, 'edit']"
                    >
                      <i class="fas fa-edit mr-2"></i>Edit
                    </a>
                    <button
                      class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-all font-medium"
                      (click)="confirmDelete = true"
                    >
                      <i class="fas fa-trash-alt mr-2"></i>Delete
                    </button>
                  </div>
                  <div class="flex items-center gap-2" *ngIf="!canEdit(r) && canModerate()">
                    <button
                      class="px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition-all font-medium"
                      (click)="toggleHidden()"
                    >
                      <i [class]="r.hidden ? 'fas fa-eye' : 'fas fa-eye-slash'" class="mr-2"></i
                      >{{ r.hidden ? 'Unhide' : 'Hide' }}
                    </button>
                    <button
                      class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-all font-medium"
                      (click)="confirmManagerDelete = true"
                    >
                      <i class="fas fa-trash-alt mr-2"></i>Delete as Manager
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Ratings Grid -->
            <div class="p-6 border-b border-neutral-200">
              <h3 class="text-lg font-semibold mb-4 text-neutral-800">
                <i class="fas fa-star text-yellow-500 mr-2"></i>Ratings Breakdown
              </h3>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div
                  class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg p-4 text-center border border-blue-200"
                >
                  <div class="text-xs text-blue-700 font-medium mb-2">Performance</div>
                  <div class="text-3xl font-bold text-blue-800" *ngIf="r.ratings.performance">
                    {{ r.ratings.performance }}<span class="text-lg text-blue-600">/10</span>
                  </div>
                  <div class="text-2xl font-semibold text-blue-400" *ngIf="!r.ratings.performance">
                    N/A
                  </div>
                </div>

                <div
                  class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-lg p-4 text-center border border-purple-200"
                >
                  <div class="text-xs text-purple-700 font-medium mb-2">Sound & Light</div>
                  <div
                    class="text-3xl font-bold text-purple-800"
                    *ngIf="r.ratings.soundAndLighting"
                  >
                    {{ r.ratings.soundAndLighting }}<span class="text-lg text-purple-600">/10</span>
                  </div>
                  <div
                    class="text-2xl font-semibold text-purple-400"
                    *ngIf="!r.ratings.soundAndLighting"
                  >
                    N/A
                  </div>
                </div>

                <div
                  class="bg-gradient-to-br from-green-50 to-green-100 rounded-lg p-4 text-center border border-green-200"
                >
                  <div class="text-xs text-green-700 font-medium mb-2">Venue</div>
                  <div class="text-3xl font-bold text-green-800" *ngIf="r.ratings.venue">
                    {{ r.ratings.venue }}<span class="text-lg text-green-600">/10</span>
                  </div>
                  <div class="text-2xl font-semibold text-green-400" *ngIf="!r.ratings.venue">
                    N/A
                  </div>
                </div>

                <div
                  class="bg-gradient-to-br from-orange-50 to-orange-100 rounded-lg p-4 text-center border border-orange-200"
                >
                  <div class="text-xs text-orange-700 font-medium mb-2">Overall</div>
                  <div
                    class="text-3xl font-bold text-orange-800"
                    *ngIf="r.ratings.overallImpression"
                  >
                    {{ r.ratings.overallImpression
                    }}<span class="text-lg text-orange-600">/10</span>
                  </div>
                  <div
                    class="text-2xl font-semibold text-orange-400"
                    *ngIf="!r.ratings.overallImpression"
                  >
                    N/A
                  </div>
                </div>
              </div>
            </div>

            <!-- Review Text -->
            <div class="p-6 bg-neutral-50">
              <div class="flex items-start gap-4">
                <div
                  class="w-12 h-12 bg-gradient-to-br from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-bold text-lg"
                >
                  {{ r.author.name.charAt(0).toUpperCase() }}
                </div>
                <div class="flex-1">
                  <div class="font-semibold text-neutral-800">{{ r.author.name }}</div>
                  <div class="text-sm text-neutral-500">{{ r.author.email }}</div>
                  <p class="mt-3 text-neutral-700 text-lg leading-relaxed italic" *ngIf="r.comment">
                    "{{ r.comment }}"
                  </p>
                  <p class="mt-3 text-neutral-500 italic" *ngIf="!r.comment">
                    <i class="fas fa-info-circle mr-2"></i>No written review provided
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Comments Section -->
          <div class="bg-white rounded-xl shadow-lg border border-neutral-200 p-6">
            <div class="flex items-center gap-3 mb-6">
              <div
                class="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-500 rounded-lg flex items-center justify-center"
              >
                <i class="fas fa-comments text-white text-lg"></i>
              </div>
              <h2 class="text-2xl font-bold text-neutral-800">Discussion</h2>
              <span
                class="ml-auto px-3 py-1 bg-neutral-100 text-neutral-600 rounded-full text-sm font-medium"
              >
                {{ comments().length }} {{ comments().length === 1 ? 'comment' : 'comments' }}
              </span>
            </div>

            <!-- Manager Comment Form -->
            <div
              *ngIf="canCommentDirectly()"
              class="mb-6 p-4 bg-blue-50 rounded-lg border-2 border-blue-200"
            >
              <div class="flex items-center gap-2 mb-3">
                <i class="fas fa-shield-alt text-blue-600"></i>
                <span class="text-sm font-medium text-blue-800">Commenting as Manager</span>
              </div>
              <textarea
                class="w-full px-4 py-3 border-2 border-blue-300 rounded-lg focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all resize-none"
                rows="4"
                [(ngModel)]="newComment"
                placeholder="Share your response to this review..."
              ></textarea>
              <div class="mt-3 flex justify-end">
                <button
                  class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all font-medium shadow-md hover:shadow-lg"
                  (click)="postComment()"
                >
                  <i class="fas fa-paper-plane mr-2"></i>Post Comment
                </button>
              </div>
            </div>

            <!-- Info for non-managers -->
            <div
              *ngIf="auth.isAuthenticated() && !canCommentDirectly()"
              class="mb-6 p-4 bg-amber-50 rounded-lg border border-amber-200"
            >
              <div class="flex items-start gap-3">
                <i class="fas fa-info-circle text-amber-600 mt-0.5"></i>
                <div class="text-sm text-amber-800">
                  <strong>Note:</strong> Only managers can comment directly on reviews. You can
                  participate by replying to manager comments below.
                </div>
              </div>
            </div>

            <!-- Comments List -->
            <div *ngIf="comments().length === 0" class="text-center py-12">
              <div
                class="w-16 h-16 bg-neutral-100 rounded-full flex items-center justify-center mx-auto mb-4"
              >
                <i class="fas fa-comment-slash text-neutral-400 text-2xl"></i>
              </div>
              <p class="text-neutral-500">No comments yet. Be the first to start the discussion!</p>
            </div>

            <div class="space-y-4">
              <app-comment-item
                *ngFor="let c of comments()"
                [comment]="c"
                (onReply)="onReply($event)"
                (onDelete)="onDelete($event)"
              ></app-comment-item>
            </div>
          </div>
        </div>

        <!-- Delete Confirmation Modal -->
        <div
          *ngIf="confirmDelete"
          class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
          (click)="confirmDelete = false"
        >
          <div
            class="bg-white rounded-xl shadow-2xl w-full max-w-md p-6"
            (click)="$event.stopPropagation()"
          >
            <div class="flex items-center gap-4 mb-4">
              <div class="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                <i class="fas fa-exclamation-triangle text-red-600 text-xl"></i>
              </div>
              <div>
                <h3 class="text-xl font-bold text-neutral-800">Delete Review?</h3>
                <p class="text-sm text-neutral-600">This action is permanent</p>
              </div>
            </div>
            <p class="text-neutral-700 mb-6">
              Are you sure you want to delete this review? All associated ratings and comments will
              be removed.
            </p>
            <div class="flex gap-3">
              <button
                class="flex-1 px-4 py-3 bg-neutral-200 text-neutral-700 rounded-lg font-medium hover:bg-neutral-300 transition-all"
                (click)="confirmDelete = false"
              >
                Cancel
              </button>
              <button
                class="flex-1 px-4 py-3 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700 transition-all"
                (click)="deleteReview()"
              >
                <i class="fas fa-trash-alt mr-2"></i>Delete
              </button>
            </div>
          </div>
        </div>

        <!-- Manager Delete Confirmation Modal -->
        <div
          *ngIf="confirmManagerDelete"
          class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
          (click)="confirmManagerDelete = false"
        >
          <div
            class="bg-white rounded-xl shadow-2xl w-full max-w-md p-6"
            (click)="$event.stopPropagation()"
          >
            <div class="flex items-center gap-4 mb-4">
              <div class="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                <i class="fas fa-shield-alt text-red-600 text-xl"></i>
              </div>
              <div>
                <h3 class="text-xl font-bold text-neutral-800">Manager Action: Delete Review</h3>
                <p class="text-sm text-neutral-600">Permanently remove this review</p>
              </div>
            </div>
            <p class="text-neutral-700 mb-6">
              As a manager, you are about to <strong>permanently delete</strong> this review. The
              review will be removed from the system and excluded from location ratings. This action
              cannot be undone.
            </p>
            <div class="flex gap-3">
              <button
                class="flex-1 px-4 py-3 bg-neutral-200 text-neutral-700 rounded-lg font-medium hover:bg-neutral-300 transition-all"
                (click)="confirmManagerDelete = false"
              >
                Cancel
              </button>
              <button
                class="flex-1 px-4 py-3 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700 transition-all"
                (click)="deleteAsManager()"
              >
                <i class="fas fa-trash-alt mr-2"></i>Delete Review
              </button>
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
  confirmManagerDelete = false;
  isManagerOfLocation = signal(false);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reviewService: ReviewService,
    private userService: UserService,
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

      // Check if current user is manager of this location
      if (this.auth.isAuthenticated() && this.auth.hasRole('ROLE_MANAGER')) {
        this.userService.getManagedLocations().subscribe((locations) => {
          // Find the location that has events matching this review's event
          // We need to check if the review's location is managed by the user
          // Since the review has event info, we can use the locationId from backend
          // For now, we'll do a simple check
          this.reviewService.getReview(this.id).subscribe((reviewDetails) => {
            // The backend should provide locationId in the review details
            // For now, we'll check if any managed location matches
            const isManager = locations.some((loc) => {
              // You might need to adjust this based on your backend response
              return true; // Simplified - adjust based on actual data structure
            });
            this.isManagerOfLocation.set(isManager);
          });
        });
      }
    });

    this.reviewService.getComments(this.id).subscribe((list) => this.comments.set(list));
  }

  canEdit(r: ReviewDetailsDTO | null): boolean {
    const email = this.auth.currentUser()?.email;
    return !!r && r.author.email === email;
  }

  canModerate(): boolean {
    return this.auth.isAuthenticated() && (this.auth.hasRole('ROLE_MANAGER') || this.auth.hasRole('ROLE_ADMIN'));
  }

  canCommentDirectly(): boolean {
    return this.auth.isAuthenticated() && (this.auth.hasRole('ROLE_MANAGER') || this.auth.hasRole('ROLE_ADMIN'));
  }

  toggleHidden() {
    const r = this.review();
    if (!r) return;
    this.reviewService.hideReview(r.id, !r.hidden).subscribe(() => this.refresh());
  }

  postComment() {
    if (!this.newComment.trim()) return;
    if (!this.canCommentDirectly()) {
      alert('Only managers can comment directly on reviews');
      return;
    }
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
    this.confirmDelete = false;
    this.reviewService.deleteReview(this.id).subscribe(() => this.router.navigate(['/me/reviews']));
  }

  deleteAsManager() {
    this.confirmManagerDelete = false;
    this.reviewService.deleteByManager(this.id).subscribe(() => {
      this.router.navigate(['/manager/reviews']);
    });
  }
}
