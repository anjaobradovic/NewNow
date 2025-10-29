import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FeedService } from '../../services/feed.service';
import { ReviewDetailsDTO } from '../../models/user.model';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-feed-popular-reviews',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent],
  template: `
    <app-navbar />
    <div class="min-h-screen bg-gradient-to-br from-neutral-50 to-primary-50/30">
      <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-4xl font-bold text-neutral-900 mb-3">Latest Reviews</h1>
          <p class="text-lg text-neutral-600">Recent feedback from our most popular venue</p>
        </div>

        <!-- Loading State -->
        @if (loading()) {
        <div class="flex justify-center items-center py-20">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
        }

        <!-- Error State -->
        @if (error()) {
        <div class="bg-red-50 border border-red-200 rounded-2xl p-6 text-center">
          <svg
            class="w-12 h-12 text-red-400 mx-auto mb-3"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <p class="text-red-700 font-medium">{{ error() }}</p>
        </div>
        }

        <!-- Reviews List -->
        @if (!loading() && !error() && reviews().length > 0) {
        <div class="space-y-6">
          @for (review of reviews(); track review.id) {
          <div
            class="bg-white rounded-2xl shadow-sm hover:shadow-md transition-shadow duration-300 overflow-hidden border border-neutral-100"
          >
            <div class="p-6">
              <!-- Header -->
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                  <div
                    class="w-12 h-12 rounded-xl bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-lg"
                  >
                    {{ getInitials(review.author.name) }}
                  </div>
                  <div>
                    <h3 class="font-semibold text-neutral-900">{{ review.author.name }}</h3>
                    <p class="text-sm text-neutral-500">{{ formatDate(review.createdAt) }}</p>
                  </div>
                </div>

                <!-- Average Rating -->
                <div class="flex items-center gap-2 bg-primary-50 px-3 py-1.5 rounded-xl">
                  <svg class="w-5 h-5 text-yellow-500 fill-current" viewBox="0 0 20 20">
                    <path
                      d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
                    />
                  </svg>
                  <span class="font-bold text-neutral-900">{{
                    review.ratings.average.toFixed(1)
                  }}</span>
                </div>
              </div>

              <!-- Event Info -->
              <div class="mb-4 p-3 bg-neutral-50 rounded-xl">
                <div class="flex items-center gap-2 text-sm text-neutral-700 mb-1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                    />
                  </svg>
                  <span class="font-medium">{{ review.event.name }}</span>
                </div>
                <div class="flex items-center gap-4 text-xs text-neutral-600">
                  <span
                    class="inline-flex items-center gap-1 px-2 py-0.5 bg-white rounded-lg border border-neutral-200"
                  >
                    <span
                      class="w-2 h-2 rounded-full"
                      [class.bg-primary-500]="review.event.recurrent"
                      [class.bg-neutral-400]="!review.event.recurrent"
                    ></span>
                    {{ review.event.recurrent ? 'Regular' : 'One-time' }}
                  </span>
                  <span>{{ review.event.type }}</span>
                  <span>Dogodilo se {{ review.eventCount }}x</span>
                </div>
              </div>

              <!-- Detailed Ratings -->
              <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
                <div class="text-center p-3 bg-neutral-50 rounded-xl">
                  <div class="text-xs text-neutral-600 mb-1">Performance</div>
                  <div class="text-lg font-bold text-neutral-900">
                    {{
                      review.ratings.performance !== null
                        ? review.ratings.performance.toFixed(1)
                        : 'N/A'
                    }}
                  </div>
                </div>
                <div class="text-center p-3 bg-neutral-50 rounded-xl">
                  <div class="text-xs text-neutral-600 mb-1">Sound & Light</div>
                  <div class="text-lg font-bold text-neutral-900">
                    {{
                      review.ratings.soundAndLighting !== null
                        ? review.ratings.soundAndLighting.toFixed(1)
                        : 'N/A'
                    }}
                  </div>
                </div>
                <div class="text-center p-3 bg-neutral-50 rounded-xl">
                  <div class="text-xs text-neutral-600 mb-1">Venue</div>
                  <div class="text-lg font-bold text-neutral-900">
                    {{ review.ratings.venue !== null ? review.ratings.venue.toFixed(1) : 'N/A' }}
                  </div>
                </div>
                <div class="text-center p-3 bg-neutral-50 rounded-xl">
                  <div class="text-xs text-neutral-600 mb-1">Overall</div>
                  <div class="text-lg font-bold text-neutral-900">
                    {{
                      review.ratings.overallImpression !== null
                        ? review.ratings.overallImpression.toFixed(1)
                        : 'N/A'
                    }}
                  </div>
                </div>
              </div>

              <!-- Comment -->
              @if (review.comment) {
              <p class="text-neutral-700 leading-relaxed mb-4">{{ review.comment }}</p>
              }

              <!-- View Details -->
              <div class="flex justify-end">
                <a
                  [routerLink]="['/reviews', review.id]"
                  class="inline-flex items-center gap-2 text-sm font-medium text-primary-600 hover:text-primary-700 hover:gap-3 transition-all"
                >
                  <span>View full review</span>
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M9 5l7 7-7 7"
                    />
                  </svg>
                </a>
              </div>
            </div>
          </div>
          }
        </div>
        }

        <!-- Empty State -->
        @if (!loading() && !error() && reviews().length === 0) {
        <div class="text-center py-20">
          <svg
            class="w-20 h-20 text-neutral-300 mx-auto mb-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
            />
          </svg>
          <p class="text-neutral-500 text-lg">No reviews available yet</p>
        </div>
        }
      </div>
    </div>
  `,
})
export class FeedPopularReviewsComponent implements OnInit {
  reviews = signal<ReviewDetailsDTO[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  constructor(private feedService: FeedService) {}

  ngOnInit(): void {
    this.loadLatestReviews();
  }

  private loadLatestReviews(): void {
    this.loading.set(true);
    this.error.set(null);

    this.feedService.getPopularLocationLatestReviews().subscribe({
      next: (data) => {
        this.reviews.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading latest reviews:', err);
        this.error.set('Unable to load latest reviews. Please try again later.');
        this.loading.set(false);
      },
    });
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map((s) => s[0])
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }

  formatDate(isoDate: string): string {
    const date = new Date(isoDate);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;

    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }
}
