import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService } from '../../services/user.service';
import { ReviewService } from '../../services/review.service';
import { ReviewDetailsDTO } from '../../models/user.model';

@Component({
  selector: 'app-manager-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 py-10">
      <div class="max-w-7xl mx-auto px-4">
        <!-- Header Section -->
        <div class="mb-8">
          <h1 class="text-4xl font-bold text-neutral-800 mb-2">
            <i class="fas fa-shield-alt mr-3 text-green-600"></i>Review Moderation
          </h1>
          <p class="text-neutral-600">
            Manage reviews for your locations - hide inappropriate content or remove reviews
            entirely
          </p>
        </div>

        <!-- Location Selector Card -->
        <div class="bg-white rounded-xl shadow-lg border border-neutral-200 p-6 mb-6">
          <div class="flex items-center gap-4">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <i class="fas fa-map-marker-alt text-green-600 text-xl"></i>
              </div>
            </div>
            <div class="flex-1">
              <label class="block text-sm font-medium text-neutral-700 mb-2">Select Location</label>
              <select
                class="w-full px-4 py-3 border-2 border-neutral-300 rounded-lg focus:border-green-500 focus:ring-2 focus:ring-green-200 transition-all"
                [(ngModel)]="selectedLocationId"
                (change)="load()"
              >
                <option [ngValue]="null" disabled>Choose a location to manage...</option>
                <option *ngFor="let l of managedLocations" [ngValue]="l.id">
                  📍 {{ l.locationName }}
                </option>
              </select>
            </div>
          </div>
          <div
            *ngIf="selectedLocationId"
            class="mt-4 p-3 bg-green-50 rounded-lg border border-green-200"
          >
            <p class="text-sm text-green-800">
              <i class="fas fa-info-circle mr-2"></i>
              <strong>Tip:</strong> Hidden reviews are not visible to public but still count towards
              rating. Removed reviews are permanently deleted and excluded from ratings.
            </p>
          </div>
        </div>

        <!-- Statistics Cards -->
        <div
          *ngIf="selectedLocationId && reviews().length > 0"
          class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6"
        >
          <div class="bg-white rounded-xl shadow-md border border-neutral-200 p-5">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-neutral-600">Total Reviews</p>
                <p class="text-3xl font-bold text-neutral-800">{{ reviews().length }}</p>
              </div>
              <div class="w-14 h-14 bg-purple-100 rounded-full flex items-center justify-center">
                <i class="fas fa-star text-purple-600 text-xl"></i>
              </div>
            </div>
          </div>
          <div class="bg-white rounded-xl shadow-md border border-neutral-200 p-5">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-neutral-600">Hidden</p>
                <p class="text-3xl font-bold text-yellow-600">{{ countHidden() }}</p>
              </div>
              <div class="w-14 h-14 bg-yellow-100 rounded-full flex items-center justify-center">
                <i class="fas fa-eye-slash text-yellow-600 text-xl"></i>
              </div>
            </div>
          </div>
          <div class="bg-white rounded-xl shadow-md border border-neutral-200 p-5">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-neutral-600">Average Rating</p>
                <p class="text-3xl font-bold text-green-600">
                  {{ calculateAverage() | number : '1.1-1' }}
                </p>
              </div>
              <div class="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center">
                <i class="fas fa-chart-line text-green-600 text-xl"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div
          *ngIf="!selectedLocationId || reviews().length === 0"
          class="bg-white rounded-xl shadow-md border border-neutral-200 p-12 text-center"
        >
          <div
            class="w-20 h-20 bg-neutral-100 rounded-full flex items-center justify-center mx-auto mb-4"
          >
            <i class="fas fa-inbox text-neutral-400 text-3xl"></i>
          </div>
          <h3 class="text-xl font-semibold text-neutral-700 mb-2">No Reviews Yet</h3>
          <p class="text-neutral-500">
            {{
              !selectedLocationId
                ? 'Select a location to view and moderate reviews'
                : 'No reviews found for this location'
            }}
          </p>
        </div>

        <!-- Reviews List -->
        <div class="space-y-4">
          <div
            class="bg-white rounded-xl shadow-md border border-neutral-200 hover:shadow-lg transition-all duration-200"
            *ngFor="let r of reviews()"
          >
            <!-- Review Header -->
            <div class="p-6 border-b border-neutral-200">
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <div class="flex items-center gap-3 mb-2">
                    <div
                      class="w-10 h-10 bg-gradient-to-br from-green-500 to-emerald-500 rounded-full flex items-center justify-center text-white font-bold"
                    >
                      {{ r.author.name.charAt(0).toUpperCase() }}
                    </div>
                    <div>
                      <h3 class="font-semibold text-neutral-800">{{ r.author.name }}</h3>
                      <p class="text-xs text-neutral-500">{{ r.author.email }}</p>
                    </div>
                  </div>
                  <div class="flex flex-wrap items-center gap-2 mt-3">
                    <span
                      class="px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800"
                    >
                      <i class="fas fa-calendar-alt mr-1"></i>{{ r.event.name }}
                    </span>
                    @if (r.eventCount > 0) {
                    <span
                      class="px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                    >
                      <i class="fas fa-repeat mr-1"></i>Dogodilo se {{ r.eventCount }}x
                    </span>
                    }
                    <span
                      *ngIf="r.hidden"
                      class="px-3 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 animate-pulse"
                    >
                      <i class="fas fa-eye-slash mr-1"></i>Hidden from Public
                    </span>
                    <span class="text-xs text-neutral-500">
                      <i class="far fa-clock mr-1"></i>{{ r.createdAt | date : 'medium' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Review Body -->
            <div class="p-6">
              <!-- Ratings Grid -->
              <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mb-4">
                <div class="text-center p-3 bg-neutral-50 rounded-lg">
                  <p class="text-xs text-neutral-600 mb-1">Performance</p>
                  <p class="text-lg font-bold text-neutral-800">
                    {{ r.ratings.performance }}<span class="text-sm text-neutral-400">/10</span>
                  </p>
                </div>
                <div class="text-center p-3 bg-neutral-50 rounded-lg">
                  <p class="text-xs text-neutral-600 mb-1">Sound & Light</p>
                  <p class="text-lg font-bold text-neutral-800">
                    {{ r.ratings.soundAndLighting
                    }}<span class="text-sm text-neutral-400">/10</span>
                  </p>
                </div>
                <div class="text-center p-3 bg-neutral-50 rounded-lg">
                  <p class="text-xs text-neutral-600 mb-1">Venue</p>
                  <p class="text-lg font-bold text-neutral-800">
                    {{ r.ratings.venue }}<span class="text-sm text-neutral-400">/10</span>
                  </p>
                </div>
                <div class="text-center p-3 bg-neutral-50 rounded-lg">
                  <p class="text-xs text-neutral-600 mb-1">Overall</p>
                  <p class="text-lg font-bold text-neutral-800">
                    {{ r.ratings.overallImpression
                    }}<span class="text-sm text-neutral-400">/10</span>
                  </p>
                </div>
                <div
                  class="text-center p-3 bg-gradient-to-br from-green-500 to-emerald-500 rounded-lg text-white"
                >
                  <p class="text-xs mb-1">Average</p>
                  <p class="text-lg font-bold">
                    {{ r.ratings.average | number : '1.1-1'
                    }}<span class="text-sm opacity-80">/10</span>
                  </p>
                </div>
              </div>

              <!-- Comment -->
              <div
                *ngIf="r.comment"
                class="p-4 bg-neutral-50 rounded-lg border-l-4 border-green-500"
              >
                <p class="text-sm text-neutral-700 italic">"{{ r.comment }}"</p>
              </div>

              <!-- Actions -->
              <div class="flex flex-wrap items-center gap-3 mt-6 pt-6 border-t border-neutral-200">
                <button
                  class="px-5 py-2.5 rounded-lg font-medium transition-all duration-200 flex items-center gap-2"
                  [class.bg-yellow-100]="r.hidden"
                  [class.text-yellow-700]="r.hidden"
                  [class.hover:bg-yellow-200]="r.hidden"
                  [class.bg-neutral-200]="!r.hidden"
                  [class.text-neutral-700]="!r.hidden"
                  [class.hover:bg-neutral-300]="!r.hidden"
                  (click)="toggleHidden(r)"
                >
                  <i [class]="r.hidden ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
                  {{ r.hidden ? 'Unhide Review' : 'Hide Review' }}
                </button>
                <button
                  class="px-5 py-2.5 bg-red-100 text-red-700 rounded-lg font-medium hover:bg-red-200 transition-all duration-200 flex items-center gap-2"
                  (click)="confirmDelete(r)"
                >
                  <i class="fas fa-trash-alt"></i>
                  Remove Permanently
                </button>
                <a
                  class="px-5 py-2.5 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition-all duration-200 flex items-center gap-2"
                  [routerLink]="['/reviews', r.id]"
                >
                  <i class="fas fa-comment-dots"></i>
                  View & Reply
                </a>
                <span class="ml-auto text-xs text-neutral-500">
                  <i class="fas fa-hashtag"></i>ID: {{ r.id }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div
      *ngIf="showDeleteModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      (click)="showDeleteModal = false"
    >
      <div
        class="bg-white rounded-xl shadow-2xl max-w-md w-full p-6"
        (click)="$event.stopPropagation()"
      >
        <div class="flex items-center gap-4 mb-4">
          <div class="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
            <i class="fas fa-exclamation-triangle text-red-600 text-xl"></i>
          </div>
          <div>
            <h3 class="text-xl font-bold text-neutral-800">Confirm Deletion</h3>
            <p class="text-sm text-neutral-600">This action cannot be undone</p>
          </div>
        </div>
        <p class="text-neutral-700 mb-6">
          Are you sure you want to <strong>permanently remove</strong> this review? The review will
          be deleted and excluded from location ratings.
        </p>
        <div class="flex gap-3">
          <button
            class="flex-1 px-4 py-2.5 bg-neutral-200 text-neutral-700 rounded-lg font-medium hover:bg-neutral-300 transition-all"
            (click)="showDeleteModal = false"
          >
            Cancel
          </button>
          <button
            class="flex-1 px-4 py-2.5 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700 transition-all"
            (click)="deleteByManager(reviewToDelete!)"
          >
            Delete Review
          </button>
        </div>
      </div>
    </div>
  `,
})
export class ManagerReviewsComponent implements OnInit {
  managedLocations: Array<{ id: number; locationName: string }> = [];
  selectedLocationId: number | null = null;
  reviews = signal<ReviewDetailsDTO[]>([]);
  showDeleteModal = false;
  reviewToDelete?: ReviewDetailsDTO;

  constructor(private userService: UserService, private reviewService: ReviewService) {}

  ngOnInit(): void {
    this.userService.getManagedLocations().subscribe({
      next: (list) => {
        console.log('Managed locations:', list);
        this.managedLocations = list;
        // Auto-select first location if available
        if (list.length > 0) {
          this.selectedLocationId = list[0].id;
          console.log('Auto-selected location ID:', this.selectedLocationId);
          this.load();
        }
      },
      error: (err) => {
        console.error('Error loading managed locations:', err);
      },
    });
  }

  load() {
    if (!this.selectedLocationId) return;
    console.log('Loading reviews for location:', this.selectedLocationId);
    this.reviewService
      .getLocationReviewsForManager(this.selectedLocationId, 'date', 'desc', 0, 20)
      .subscribe({
        next: (res) => {
          console.log('Received reviews response:', res);
          console.log('Reviews content:', res.content);
          this.reviews.set(res.content || []);
        },
        error: (err) => {
          console.error('Error loading reviews:', err);
          this.reviews.set([]);
        },
      });
  }

  toggleHidden(r: ReviewDetailsDTO) {
    this.reviewService.hideReview(r.id, !r.hidden).subscribe(() => this.load());
  }

  confirmDelete(r: ReviewDetailsDTO) {
    this.reviewToDelete = r;
    this.showDeleteModal = true;
  }

  deleteByManager(r: ReviewDetailsDTO) {
    this.showDeleteModal = false;
    this.reviewService.deleteByManager(r.id).subscribe(() => this.load());
  }

  countHidden(): number {
    return this.reviews().filter((r) => r.hidden).length;
  }

  calculateAverage(): number {
    const reviews = this.reviews();
    if (reviews.length === 0) return 0;
    const sum = reviews.reduce((acc, r) => acc + r.ratings.average, 0);
    return sum / reviews.length;
  }
}
