import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AnalyticsService } from '../../services/analytics.service';
import { LocationService } from '../../services/location.service';
import { LocationSummaryDTO, EventCountsDTO, TopRatingsDTO } from '../../models/analytics.model';
import { ReviewDTO } from '../../models/user.model';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, NavbarComponent],
  template: `
    <app-navbar />
    <div class="min-h-screen bg-gradient-to-br from-neutral-50 to-primary-50/30">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <!-- Header -->
        <div class="mb-8">
          <div class="flex items-center gap-3 mb-3">
            <a
              [routerLink]="['/locations', locationId()]"
              class="text-primary-600 hover:text-primary-700"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M10 19l-7-7m0 0l7-7m-7 7h18"
                />
              </svg>
            </a>
            <h1 class="text-4xl font-bold text-neutral-900">Performance Insights</h1>
          </div>
          @if (summary()) {
          <p class="text-lg text-neutral-600">{{ summary()!.locationName }}</p>
          }
        </div>

        <!-- Period Selector -->
        <div class="bg-white rounded-2xl shadow-sm border border-neutral-100 p-6 mb-6">
          <div class="flex flex-wrap items-end gap-4">
            <div class="flex-1 min-w-[200px]">
              <label class="block text-sm font-medium text-neutral-700 mb-2">Time Period</label>
              <select
                [(ngModel)]="selectedPeriod"
                (ngModelChange)="onPeriodChange()"
                class="w-full px-4 py-2.5 border border-neutral-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="weekly">Last Week</option>
                <option value="monthly">Last Month</option>
                <option value="yearly">Last Year</option>
                <option value="custom">Custom Range</option>
              </select>
            </div>

            @if (selectedPeriod === 'custom') {
            <div class="flex-1 min-w-[200px]">
              <label class="block text-sm font-medium text-neutral-700 mb-2">Start Date</label>
              <input
                type="date"
                [(ngModel)]="startDate"
                class="w-full px-4 py-2.5 border border-neutral-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
            <div class="flex-1 min-w-[200px]">
              <label class="block text-sm font-medium text-neutral-700 mb-2">End Date</label>
              <input
                type="date"
                [(ngModel)]="endDate"
                class="w-full px-4 py-2.5 border border-neutral-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
            <button (click)="loadSummary()" class="btn-primary">Apply</button>
            }
          </div>
        </div>

        <!-- Loading State -->
        @if (loading()) {
        <div class="flex justify-center items-center py-20">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
        }

        <!-- Error State -->
        @if (error()) {
        <div class="bg-red-50 border border-red-200 rounded-2xl p-6 text-center mb-6">
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
        } @if (!loading() && !error()) {
        <!-- Summary Cards -->
        @if (summary()) {
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div class="bg-white rounded-2xl shadow-sm border border-neutral-100 p-6">
            <div class="flex items-center justify-between mb-2">
              <div class="w-12 h-12 rounded-xl bg-primary-100 flex items-center justify-center">
                <svg
                  class="w-6 h-6 text-primary-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
              </div>
            </div>
            <div class="text-3xl font-bold text-neutral-900 mb-1">
              {{ summary()!.totalEvents }}
            </div>
            <div class="text-sm text-neutral-600">Total Events</div>
          </div>

          <div class="bg-white rounded-2xl shadow-sm border border-neutral-100 p-6">
            <div class="flex items-center justify-between mb-2">
              <div class="w-12 h-12 rounded-xl bg-yellow-100 flex items-center justify-center">
                <svg
                  class="w-6 h-6 text-yellow-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"
                  />
                </svg>
              </div>
            </div>
            <div class="text-3xl font-bold text-neutral-900 mb-1">
              {{ summary()!.averageRating?.toFixed(1) || 'N/A' }}
            </div>
            <div class="text-sm text-neutral-600">Average Rating</div>
          </div>

          <div class="bg-white rounded-2xl shadow-sm border border-neutral-100 p-6">
            <div class="flex items-center justify-between mb-2">
              <div class="w-12 h-12 rounded-xl bg-green-100 flex items-center justify-center">
                <svg
                  class="w-6 h-6 text-green-600"
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
              </div>
            </div>
            <div class="text-3xl font-bold text-neutral-900 mb-1">
              {{ summary()!.totalReviews }}
            </div>
            <div class="text-sm text-neutral-600">Total Reviews</div>
          </div>

          <div class="bg-white rounded-2xl shadow-sm border border-neutral-100 p-6">
            <div class="flex items-center justify-between mb-2">
              <div class="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center">
                <svg
                  class="w-6 h-6 text-blue-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                  />
                </svg>
              </div>
            </div>
            <div class="text-3xl font-bold text-neutral-900 mb-1">
              {{ summary()!.totalVisitors }}
            </div>
            <div class="text-sm text-neutral-600">Total Visitors</div>
          </div>
        </div>
        }

        <!-- Event Counts -->
        @if (eventCounts()) {
        <div class="bg-white rounded-2xl shadow-sm border border-neutral-100 p-6 mb-8">
          <h2 class="text-xl font-bold text-neutral-900 mb-6">Event Breakdown</h2>
          <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
            <div class="text-center p-4 bg-neutral-50 rounded-xl">
              <div class="text-2xl font-bold text-neutral-900 mb-1">
                {{ eventCounts()!.totalEvents }}
              </div>
              <div class="text-xs text-neutral-600">All Events</div>
            </div>
            <div class="text-center p-4 bg-primary-50 rounded-xl">
              <div class="text-2xl font-bold text-primary-700 mb-1">
                {{ eventCounts()!.regularEvents }}
              </div>
              <div class="text-xs text-neutral-600">Regular</div>
            </div>
            <div class="text-center p-4 bg-purple-50 rounded-xl">
              <div class="text-2xl font-bold text-purple-700 mb-1">
                {{ eventCounts()!.nonRegularEvents }}
              </div>
              <div class="text-xs text-neutral-600">One-time</div>
            </div>
            <div class="text-center p-4 bg-green-50 rounded-xl">
              <div class="text-2xl font-bold text-green-700 mb-1">
                {{ eventCounts()!.freeEvents }}
              </div>
              <div class="text-xs text-neutral-600">Free</div>
            </div>
            <div class="text-center p-4 bg-orange-50 rounded-xl">
              <div class="text-2xl font-bold text-orange-700 mb-1">
                {{ eventCounts()!.paidEvents }}
              </div>
              <div class="text-xs text-neutral-600">Paid</div>
            </div>
          </div>
        </div>
        }

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <!-- Top Rated Events -->
          @if (topRatings()) {
          <div class="bg-white rounded-2xl shadow-sm border border-neutral-100 p-6">
            <h2 class="text-xl font-bold text-neutral-900 mb-6">Top Rated Events</h2>
            @if (topRatings()!.topEvents.length > 0) {
            <div class="space-y-3">
              @for (event of topRatings()!.topEvents; track event.eventId) {
              <div
                class="flex items-center justify-between p-4 bg-neutral-50 rounded-xl hover:bg-neutral-100 transition-colors"
              >
                <div class="flex-1 min-w-0">
                  <h3 class="font-semibold text-neutral-900 truncate mb-1">
                    {{ event.eventName }}
                  </h3>
                  <p class="text-sm text-neutral-600">{{ event.reviewCount }} reviews</p>
                </div>
                <div class="flex items-center gap-2 ml-4">
                  <svg class="w-5 h-5 text-yellow-500 fill-current" viewBox="0 0 20 20">
                    <path
                      d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
                    />
                  </svg>
                  <span class="text-xl font-bold text-neutral-900">{{
                    event.averageRating.toFixed(1)
                  }}</span>
                </div>
              </div>
              }
            </div>
            } @else {
            <p class="text-neutral-500 text-center py-8">No ratings available yet</p>
            }
          </div>
          }

          <!-- Latest Reviews -->
          @if (latestReviews() && latestReviews()!.length > 0) {
          <div class="bg-white rounded-2xl shadow-sm border border-neutral-100 p-6">
            <h2 class="text-xl font-bold text-neutral-900 mb-6">Recent Feedback</h2>
            <div class="space-y-4">
              @for (review of latestReviews()!; track review.id) {
              <a
                [routerLink]="['/reviews', review.id]"
                class="block p-4 bg-neutral-50 rounded-xl hover:bg-neutral-100 transition-colors"
              >
                <div class="flex items-start justify-between mb-2">
                  <div class="flex-1">
                    <h3 class="font-semibold text-neutral-900 mb-1">{{ review.eventName }}</h3>
                    <p class="text-sm text-neutral-600">{{ formatDate(review.createdAt) }}</p>
                  </div>
                  <div class="flex items-center gap-1.5 ml-4">
                    <svg class="w-4 h-4 text-yellow-500 fill-current" viewBox="0 0 20 20">
                      <path
                        d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
                      />
                    </svg>
                    <span class="font-bold text-neutral-900">{{ averageRating(review) }}</span>
                  </div>
                </div>
                <div class="flex items-center gap-2 text-xs text-neutral-600">
                  <span>Attended {{ review.eventCount }}x</span>
                </div>
              </a>
              }
            </div>
          </div>
          }
        </div>
        }
      </div>
    </div>
  `,
})
export class AnalyticsDashboardComponent implements OnInit {
  locationId = signal<number>(0);
  summary = signal<LocationSummaryDTO | null>(null);
  eventCounts = signal<EventCountsDTO | null>(null);
  topRatings = signal<TopRatingsDTO | null>(null);
  latestReviews = signal<ReviewDTO[] | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  selectedPeriod: 'weekly' | 'monthly' | 'yearly' | 'custom' = 'monthly';
  startDate: string = '';
  endDate: string = '';

  constructor(
    private route: ActivatedRoute,
    private analyticsService: AnalyticsService,
    private locationService: LocationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.locationId.set(+id);
      this.loadAllData();
    }
  }

  onPeriodChange(): void {
    if (this.selectedPeriod !== 'custom') {
      this.loadSummary();
    }
  }

  private loadAllData(): void {
    this.loadSummary();
    this.loadEventCounts();
    this.loadTopRatings();
    this.loadLatestReviews();
  }

  loadSummary(): void {
    const id = this.locationId();
    if (!id) return;

    this.loading.set(true);
    this.error.set(null);

    const start = this.selectedPeriod === 'custom' ? this.startDate : undefined;
    const end = this.selectedPeriod === 'custom' ? this.endDate : undefined;

    this.analyticsService.getLocationSummary(id, this.selectedPeriod, start, end).subscribe({
      next: (data) => {
        this.summary.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading summary:', err);
        this.error.set('Unable to load analytics data. Please try again later.');
        this.loading.set(false);
      },
    });
  }

  private loadEventCounts(): void {
    const id = this.locationId();
    if (!id) return;

    this.analyticsService.getEventCounts(id).subscribe({
      next: (data) => {
        this.eventCounts.set(data);
      },
      error: (err) => {
        console.error('Error loading event counts:', err);
      },
    });
  }

  private loadTopRatings(): void {
    const id = this.locationId();
    if (!id) return;

    this.analyticsService.getTopRatings(id, 5, 'desc').subscribe({
      next: (data) => {
        this.topRatings.set(data);
      },
      error: (err) => {
        console.error('Error loading top ratings:', err);
      },
    });
  }

  private loadLatestReviews(): void {
    const id = this.locationId();
    if (!id) return;

    this.analyticsService.getLatestReviews(id).subscribe({
      next: (data) => {
        this.latestReviews.set(data);
      },
      error: (err) => {
        console.error('Error loading latest reviews:', err);
      },
    });
  }

  averageRating(review: ReviewDTO): string {
    return review.rate.averageRating.toFixed(1);
  }

  formatDate(isoDate: string): string {
    const date = new Date(isoDate);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;

    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }
}
