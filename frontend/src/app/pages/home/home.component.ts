import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event, Location } from '../../models/event.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <!-- Hero Section -->
      <section class="relative overflow-hidden">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div class="text-center animate-fade-in">
            <h1 class="text-5xl md:text-6xl font-bold text-neutral-900 mb-6">
              Discover Amazing
              <span
                class="block text-transparent bg-clip-text bg-gradient-to-r from-primary-600 to-primary-800"
              >
                Events Around You
              </span>
            </h1>
            <p class="text-xl text-neutral-600 mb-8 max-w-2xl mx-auto">
              Find and explore the best events, venues, and experiences in your city
            </p>
            <div class="flex justify-center space-x-4">
              <a routerLink="/events" class="btn-primary text-lg"> Explore Events </a>
              <a routerLink="/locations" class="btn-secondary text-lg"> Browse Locations </a>
            </div>
          </div>
        </div>

        <!-- Autumn decoration -->
        <div class="absolute top-20 right-10 opacity-20">
          <svg class="w-32 h-32 text-autumn-rust" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M17,8C8,10 5.9,16.17 3.82,21.34L5.71,22L6.66,19.7C7.14,19.87 7.64,20 8,20C19,20 22,3 22,3C21,5 14,5.25 9,6.25C4,7.25 2,11.5 2,13.5C2,15.5 3.75,17.25 3.75,17.25C7,8 17,8 17,8Z"
            />
          </svg>
        </div>
        <div class="absolute bottom-10 left-10 opacity-10">
          <svg class="w-40 h-40 text-autumn-terracotta" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M17,8C8,10 5.9,16.17 3.82,21.34L5.71,22L6.66,19.7C7.14,19.87 7.64,20 8,20C19,20 22,3 22,3C21,5 14,5.25 9,6.25C4,7.25 2,11.5 2,13.5C2,15.5 3.75,17.25 3.75,17.25C7,8 17,8 17,8Z"
            />
          </svg>
        </div>
      </section>

      <!-- Today's Events -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h2 class="text-3xl font-bold text-neutral-900">Today's Events</h2>
            <p class="text-neutral-600 mt-2">Happening right now in your area</p>
          </div>
          <a routerLink="/events" class="text-primary-600 hover:text-primary-700 font-medium">
            View all →
          </a>
        </div>

        @if (loadingEvents()) {
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          @for (i of [1,2,3]; track i) {
          <div class="card h-80 animate-pulse">
            <div class="h-48 bg-neutral-200"></div>
            <div class="p-6 space-y-3">
              <div class="h-4 bg-neutral-200 rounded w-3/4"></div>
              <div class="h-4 bg-neutral-200 rounded w-1/2"></div>
            </div>
          </div>
          }
        </div>
        } @else if (todayEvents().length > 0) {
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 animate-slide-up">
          @for (event of todayEvents(); track event.id) {
          <div
            class="card group cursor-pointer transform hover:scale-105 transition-transform duration-300"
          >
            <div
              class="relative h-48 bg-gradient-to-br from-primary-100 to-primary-200 overflow-hidden"
            >
              @if (event.imageUrl) {
              <img [src]="event.imageUrl" [alt]="event.name" class="w-full h-full object-cover" />
              } @else {
              <div class="w-full h-full flex items-center justify-center">
                <svg
                  class="w-16 h-16 text-primary-400"
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
              } @if (event.price === 0) {
              <div
                class="absolute top-4 right-4 bg-primary-600 text-white px-3 py-1 rounded-full text-sm font-medium"
              >
                Free
              </div>
              }
            </div>
            <div class="p-6">
              <div class="flex items-center space-x-2 text-sm text-neutral-500 mb-2">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
                <span>{{ event.locationName }}</span>
              </div>
              <h3
                class="text-xl font-bold text-neutral-900 mb-2 group-hover:text-primary-600 transition-colors"
              >
                {{ event.name }}
              </h3>
              <p class="text-neutral-600 text-sm mb-3">{{ event.address }}</p>
              <div class="flex items-center justify-between">
                <span
                  class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-primary-100 text-primary-700"
                >
                  {{ event.type }}
                </span>
                @if (event.price > 0) {
                <span class="text-lg font-bold text-neutral-900">\${{ event.price }}</span>
                }
              </div>
            </div>
          </div>
          }
        </div>
        } @else {
        <div class="card p-12 text-center">
          <svg
            class="w-16 h-16 text-neutral-300 mx-auto mb-4"
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
          <p class="text-neutral-500 text-lg">No events scheduled for today</p>
        </div>
        }
      </section>

      <!-- Popular Locations -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h2 class="text-3xl font-bold text-neutral-900">Popular Locations</h2>
            <p class="text-neutral-600 mt-2">Most loved venues in your city</p>
          </div>
          <a routerLink="/locations" class="text-primary-600 hover:text-primary-700 font-medium">
            View all →
          </a>
        </div>

        @if (loadingLocations()) {
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          @for (i of [1,2,3]; track i) {
          <div class="card h-96 animate-pulse">
            <div class="h-64 bg-neutral-200"></div>
            <div class="p-6 space-y-3">
              <div class="h-4 bg-neutral-200 rounded w-3/4"></div>
              <div class="h-4 bg-neutral-200 rounded w-1/2"></div>
            </div>
          </div>
          }
        </div>
        } @else if (popularLocations().length > 0) {
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 animate-slide-up">
          @for (location of popularLocations(); track location.id) {
          <div
            class="card group cursor-pointer transform hover:scale-105 transition-transform duration-300"
          >
            <div
              class="relative h-64 bg-gradient-to-br from-primary-100 to-primary-200 overflow-hidden"
            >
              @if (location.imageUrl) {
              <img
                [src]="location.imageUrl"
                [alt]="location.name"
                class="w-full h-full object-cover"
              />
              } @else {
              <div class="w-full h-full flex items-center justify-center">
                <svg
                  class="w-20 h-20 text-primary-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                  />
                </svg>
              </div>
              }
              <div
                class="absolute top-4 right-4 bg-white/90 backdrop-blur px-3 py-1 rounded-full flex items-center space-x-1"
              >
                <svg class="w-4 h-4 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
                  <path
                    d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
                  />
                </svg>
                <span class="font-semibold text-neutral-900">{{
                  location.totalRating.toFixed(1)
                }}</span>
              </div>
            </div>
            <div class="p-6">
              <h3
                class="text-xl font-bold text-neutral-900 mb-2 group-hover:text-primary-600 transition-colors"
              >
                {{ location.name }}
              </h3>
              <p class="text-neutral-600 text-sm mb-3 line-clamp-2">{{ location.description }}</p>
              <div class="flex items-center space-x-2 text-sm text-neutral-500">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
                <span>{{ location.address }}</span>
              </div>
              <div class="mt-3">
                <span
                  class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-primary-100 text-primary-700"
                >
                  {{ location.type }}
                </span>
              </div>
            </div>
          </div>
          }
        </div>
        } @else {
        <div class="card p-12 text-center">
          <svg
            class="w-16 h-16 text-neutral-300 mx-auto mb-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
            />
          </svg>
          <p class="text-neutral-500 text-lg">No locations available</p>
        </div>
        }
      </section>
    </div>
  `,
  styles: [
    `
      .line-clamp-2 {
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    `,
  ],
})
export class HomeComponent implements OnInit {
  todayEvents = signal<Event[]>([]);
  popularLocations = signal<Location[]>([]);
  loadingEvents = signal(true);
  loadingLocations = signal(true);

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.loadTodayEvents();
    this.loadPopularLocations();
  }

  loadTodayEvents(): void {
    this.eventService.getTodayEvents().subscribe({
      next: (events) => {
        this.todayEvents.set(events);
        this.loadingEvents.set(false);
      },
      error: () => {
        this.loadingEvents.set(false);
      },
    });
  }

  loadPopularLocations(): void {
    this.eventService.getPopularLocations().subscribe({
      next: (locations) => {
        this.popularLocations.set(locations);
        this.loadingLocations.set(false);
      },
      error: () => {
        this.loadingLocations.set(false);
      },
    });
  }
}
