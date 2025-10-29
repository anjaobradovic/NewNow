import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FeedService } from '../../services/feed.service';
import { LocationDTO } from '../../models/location.model';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-feed-popular-locations',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent],
  template: `
    <app-navbar />
    <div class="min-h-screen bg-gradient-to-br from-neutral-50 to-primary-50/30">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-4xl font-bold text-neutral-900 mb-3">Popular Destinations</h1>
          <p class="text-lg text-neutral-600">Discover the most loved venues in our community</p>
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

        <!-- Locations Grid -->
        @if (!loading() && !error() && locations().length > 0) {
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          @for (location of locations(); track location.id) {
          <a
            [routerLink]="['/locations', location.id]"
            class="group bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden border border-neutral-100"
          >
            <!-- Image -->
            <div
              class="relative h-56 overflow-hidden bg-gradient-to-br from-primary-100 to-primary-200"
            >
              @if (location.imageUrl) {
              <img
                [src]="imageSrc(location.imageUrl)"
                [alt]="location.name"
                class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
              />
              } @else {
              <div class="w-full h-full flex items-center justify-center">
                <svg
                  class="w-20 h-20 text-primary-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                  />
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
              </div>
              }

              <!-- Rating Badge -->
              @if (location.totalRating > 0) {
              <div
                class="absolute top-4 right-4 bg-white/95 backdrop-blur-sm px-3 py-1.5 rounded-xl shadow-lg"
              >
                <div class="flex items-center gap-1.5">
                  <svg class="w-4 h-4 text-yellow-500 fill-current" viewBox="0 0 20 20">
                    <path
                      d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
                    />
                  </svg>
                  <span class="font-bold text-neutral-900">{{
                    location.totalRating.toFixed(1)
                  }}</span>
                </div>
              </div>
              }
            </div>

            <!-- Content -->
            <div class="p-6">
              <div class="mb-3">
                <span
                  class="inline-block px-3 py-1 bg-primary-50 text-primary-700 text-xs font-semibold rounded-lg mb-3"
                >
                  {{ location.type }}
                </span>
                <h3
                  class="text-xl font-bold text-neutral-900 group-hover:text-primary-600 transition-colors mb-2"
                >
                  {{ location.name }}
                </h3>
                <div class="flex items-start gap-2 text-neutral-600">
                  <svg
                    class="w-5 h-5 flex-shrink-0 mt-0.5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                    />
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                  </svg>
                  <span class="text-sm">{{ location.address }}</span>
                </div>
              </div>

              @if (location.description) {
              <p class="text-neutral-600 text-sm line-clamp-2 mb-4">
                {{ location.description }}
              </p>
              }

              <!-- Action -->
              <div
                class="flex items-center text-primary-600 font-medium text-sm group-hover:gap-3 gap-2 transition-all"
              >
                <span>Explore venue</span>
                <svg
                  class="w-4 h-4 group-hover:translate-x-1 transition-transform"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 5l7 7-7 7"
                  />
                </svg>
              </div>
            </div>
          </a>
          }
        </div>
        }

        <!-- Empty State -->
        @if (!loading() && !error() && locations().length === 0) {
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
              d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
            />
          </svg>
          <p class="text-neutral-500 text-lg">No popular locations found</p>
        </div>
        }
      </div>
    </div>
  `,
})
export class FeedPopularLocationsComponent implements OnInit {
  locations = signal<LocationDTO[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  constructor(private feedService: FeedService) {}

  ngOnInit(): void {
    this.loadPopularLocations();
  }

  private loadPopularLocations(): void {
    this.loading.set(true);
    this.error.set(null);

    this.feedService.getPopularLocations(12).subscribe({
      next: (data) => {
        this.locations.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading popular locations:', err);
        this.error.set('Unable to load popular locations. Please try again later.');
        this.loading.set(false);
      },
    });
  }

  imageSrc(url?: string): string {
    if (!url) return '/assets/placeholder.jpg';
    if (url.startsWith('http')) return url;
    // In development, prefix with backend URL
    const isDev = !window.location.origin.includes('production');
    return isDev ? `http://localhost:8080${url}` : url;
  }
}
