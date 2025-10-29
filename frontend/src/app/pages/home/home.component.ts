import {
  Component,
  OnInit,
  signal,
  ViewChildren,
  QueryList,
  ElementRef,
  AfterViewInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event, Location } from '../../models/event.model';
import { LocationService } from '../../services/location.service';
import { ReviewDTO } from '../../models/user.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <!-- Hero Section with Video Background -->
      <section class="relative overflow-hidden pt-10 pb-16 min-h-[500px] flex items-center">
        <!-- Video Background with rotation and fade -->
        <div class="absolute inset-0 z-0">
          @for (video of videos; track video; let i = $index) {
          <video
            #videoElement
            [class.opacity-100]="currentVideoIndex() === i"
            [class.opacity-0]="currentVideoIndex() !== i"
            [class.pointer-events-none]="currentVideoIndex() !== i"
            [muted]="true"
            [autoplay]="i === 0"
            [loop]="true"
            playsinline
            preload="auto"
            class="absolute w-full h-full object-cover transition-opacity duration-1000 ease-in-out"
            (loadeddata)="onVideoLoaded($event, i)"
            (canplay)="onVideoCanPlay($event, i)"
          >
            <source [src]="video" type="video/mp4" />
          </video>
          }
          <!-- Darker overlay for better readability -->
          <div
            class="absolute inset-0 bg-gradient-to-br from-neutral-900/80 via-neutral-900/75 to-neutral-800/80 z-10"
          ></div>
        </div>

        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div class="text-center animate-fade-in">
            <h1
              class="text-4xl md:text-5xl font-bold text-white mb-4 leading-tight drop-shadow-2xl"
            >
              Discover Amazing
              <span
                class="block text-transparent bg-clip-text bg-gradient-to-r from-primary-400 via-primary-300 to-primary-500 mt-2"
              >
                Events Around You
              </span>
            </h1>
            <p class="text-lg text-neutral-100 mb-8 max-w-2xl mx-auto drop-shadow-lg">
              Find and explore the best events, venues, and experiences in your city
            </p>
            <div class="flex flex-col sm:flex-row justify-center gap-3 items-center">
              <a
                routerLink="/events"
                class="inline-flex items-center justify-center px-6 py-2.5 text-base font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-lg hover:shadow-xl transition-all duration-200 w-full sm:w-auto"
              >
                Explore Events
              </a>
              <a
                routerLink="/locations"
                class="inline-flex items-center justify-center px-6 py-2.5 text-base font-semibold rounded-xl bg-white hover:bg-neutral-50 text-neutral-900 shadow-lg hover:shadow-xl transition-all duration-200 w-full sm:w-auto"
              >
                Browse Locations
              </a>
            </div>
          </div>
        </div>
      </section>

      <!-- Today's Events -->
      <section class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 overflow-hidden">
        <!-- Floating decorative elements - soft background elements -->
        <div
          class="absolute -top-10 right-10 opacity-[0.03] hidden lg:block -z-10"
          style="animation: float 6s ease-in-out infinite;"
        >
          <svg class="w-32 h-32 text-autumn-cream" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M17,8C8,10 5.9,16.17 3.82,21.34L5.71,22L6.66,19.7C7.14,19.87 7.64,20 8,20C19,20 22,3 22,3C21,5 14,5.25 9,6.25C4,7.25 2,11.5 2,13.5C2,15.5 3.75,17.25 3.75,17.25C7,8 17,8 17,8Z"
            />
          </svg>
        </div>
        <div
          class="absolute top-1/2 left-10 opacity-[0.03] hidden lg:block -z-10"
          style="animation: float 7s ease-in-out infinite 1s;"
        >
          <svg class="w-36 h-36 text-autumn-cream" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M17,8C8,10 5.9,16.17 3.82,21.34L5.71,22L6.66,19.7C7.14,19.87 7.64,20 8,20C19,20 22,3 22,3C21,5 14,5.25 9,6.25C4,7.25 2,11.5 2,13.5C2,15.5 3.75,17.25 3.75,17.25C7,8 17,8 17,8Z"
            />
          </svg>
        </div>
        <div
          class="absolute top-40 left-1/4 opacity-[0.02] hidden lg:block -z-10"
          style="animation: float 8s ease-in-out infinite 0.5s;"
        >
          <svg class="w-24 h-24 text-autumn-cream" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M12,17.27L18.18,21L16.54,13.97L22,9.24L14.81,8.62L12,2L9.19,8.62L2,9.24L7.45,13.97L5.82,21L12,17.27Z"
            />
          </svg>
        </div>
        <div
          class="absolute bottom-20 right-1/4 opacity-[0.03] hidden lg:block -z-10"
          style="animation: float 9s ease-in-out infinite 2s;"
        >
          <svg class="w-20 h-20 text-autumn-cream" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M17,8C8,10 5.9,16.17 3.82,21.34L5.71,22L6.66,19.7C7.14,19.87 7.64,20 8,20C19,20 22,3 22,3C21,5 14,5.25 9,6.25C4,7.25 2,11.5 2,13.5C2,15.5 3.75,17.25 3.75,17.25C7,8 17,8 17,8Z"
            />
          </svg>
        </div>

        <div class="flex items-center justify-between mb-6 relative z-10">
          <div>
            <div class="flex items-center space-x-3 mb-1">
              <svg
                class="w-8 h-8 text-primary-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="1.5"
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
              <h2 class="text-3xl font-bold text-neutral-900">Today's Events</h2>
            </div>
            <p class="text-base text-neutral-600 ml-11">Don't miss out on what's happening today</p>
          </div>
          <a
            routerLink="/events"
            class="text-primary-600 hover:text-primary-700 font-semibold flex items-center space-x-1 group"
          >
            <span>View all</span>
            <span class="transform group-hover:translate-x-1 transition-transform">→</span>
          </a>
        </div>

        @if (loadingEvents()) {
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          @for (i of [1,2,3]; track i) {
          <div class="card h-80 animate-pulse">
            <div class="h-44 bg-neutral-200"></div>
            <div class="p-4 space-y-2">
              <div class="h-3 bg-neutral-200 rounded-full w-3/4"></div>
              <div class="h-3 bg-neutral-200 rounded-full w-1/2"></div>
            </div>
          </div>
          }
        </div>
        } @else if (todayEvents().length > 0) {
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 animate-slide-up">
          @for (event of todayEvents(); track event.id) {
          <a
            [routerLink]="['/events', event.id]"
            class="card group cursor-pointer transform hover:scale-105 hover:-rotate-1 transition-all duration-300 block"
          >
            <div
              class="relative h-44 bg-gradient-to-br from-primary-100 to-primary-200 overflow-hidden"
            >
              @if (event.imageUrl) {
              <img
                [src]="event.imageUrl"
                [alt]="event.name"
                class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
              />
              } @else {
              <div
                class="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-200 to-primary-300"
              >
                <svg
                  class="w-16 h-16 text-white opacity-60"
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
                class="absolute top-3 right-3 bg-gradient-to-r from-green-400 to-green-500 text-white px-3 py-1 rounded-full text-xs font-bold shadow-lg"
              >
                Free
              </div>
              }
            </div>
            <div class="p-4">
              <div class="flex items-center space-x-2 text-xs text-neutral-500 mb-2">
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
                <span class="font-medium">{{ event.locationName }}</span>
              </div>
              <h3
                class="text-lg font-bold text-neutral-900 mb-2 group-hover:text-primary-600 transition-colors line-clamp-2"
              >
                {{ event.name }}
              </h3>
              <p class="text-neutral-600 text-xs mb-3 line-clamp-1">{{ event.address }}</p>
              <div class="flex items-center justify-between">
                <span
                  class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-gradient-to-r from-primary-100 to-primary-200 text-primary-700 border border-primary-300"
                >
                  {{ event.type }}
                </span>
                @if (event.price > 0) {
                <span
                  class="text-lg font-bold bg-gradient-to-r from-primary-600 to-primary-800 bg-clip-text text-transparent"
                  >\${{ event.price }}</span
                >
                }
              </div>
            </div>
          </a>
          }
        </div>
        } @else {
        <div class="card p-16 text-center">
          <svg
            class="w-24 h-24 text-neutral-300 mx-auto mb-4"
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
          <p class="text-neutral-500 text-lg font-medium">No events scheduled for today</p>
          <p class="text-neutral-400 text-sm mt-2">Check back later for exciting updates!</p>
        </div>
        }
      </section>

      <!-- Popular Locations -->
      <section class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="flex items-center justify-between mb-6">
          <div>
            <div class="flex items-center space-x-3 mb-1">
              <svg
                class="w-8 h-8 text-primary-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="1.5"
                  d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                ></path>
              </svg>
              <h2 class="text-3xl font-bold text-neutral-900">Popular Locations</h2>
            </div>
            <p class="text-base text-neutral-600 ml-11">Most loved venues in your city</p>
          </div>
          <a
            routerLink="/locations"
            class="text-primary-600 hover:text-primary-700 font-semibold flex items-center space-x-1 group"
          >
            <span>View all</span>
            <span class="transform group-hover:translate-x-1 transition-transform">→</span>
          </a>
        </div>

        @if (loadingLocations()) {
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          @for (i of [1,2,3]; track i) {
          <div class="card h-[400px] animate-pulse">
            <div class="h-64 bg-neutral-200"></div>
            <div class="p-6 space-y-3">
              <div class="h-4 bg-neutral-200 rounded-full w-3/4"></div>
              <div class="h-4 bg-neutral-200 rounded-full w-1/2"></div>
            </div>
          </div>
          }
        </div>
        } @else if (popularLocations().length > 0) {
        <div class="space-y-6 animate-slide-up">
          @for (location of popularLocations(); track location.id) {
          <div class="bg-white rounded-2xl shadow-md overflow-hidden border border-neutral-100">
            <!-- Location Card - Horizontal compact layout -->
            <div class="grid grid-cols-1 md:grid-cols-5 gap-0">
              <!-- Location Image - Smaller -->
              <div
                class="relative h-48 md:h-auto md:col-span-2 bg-gradient-to-br from-primary-100 to-primary-200 overflow-hidden"
              >
                @if (location.imageUrl) {
                <img
                  [src]="imageSrc(location.imageUrl)"
                  [alt]="location.name"
                  class="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
                />
                } @else {
                <div
                  class="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-100 to-autumn-sand"
                >
                  <svg
                    class="w-16 h-16 text-primary-400 opacity-80"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="1.5"
                      d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                    ></path>
                  </svg>
                </div>
                } @if (location.totalRating && location.totalRating > 0) {
                <div
                  class="absolute top-3 right-3 bg-white/95 backdrop-blur-sm px-3 py-1.5 rounded-xl flex items-center space-x-1.5 shadow-md"
                >
                  <svg class="w-4 h-4 text-yellow-500" fill="currentColor" viewBox="0 0 24 24">
                    <path
                      d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
                    ></path>
                  </svg>
                  <span class="font-bold text-neutral-900 text-sm">{{
                    location.totalRating.toFixed(1)
                  }}</span>
                </div>
                }
              </div>

              <!-- Location Info & Reviews Combined -->
              <div class="p-4 md:col-span-3 flex flex-col">
                <!-- Location Details - Compact -->
                <div class="mb-3">
                  <a [routerLink]="['/locations', location.id]">
                    <h3
                      class="text-lg font-bold text-neutral-900 mb-1 hover:text-primary-600 transition-colors line-clamp-1"
                    >
                      {{ location.name }}
                    </h3>
                  </a>
                  <div class="flex items-center space-x-2 text-xs text-neutral-500 mb-2">
                    <svg
                      class="w-3 h-3 flex-shrink-0"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                      ></path>
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                      ></path>
                    </svg>
                    <span class="font-medium line-clamp-1">{{ location.address }}</span>
                    <span
                      class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-primary-100 text-primary-700"
                    >
                      {{ location.type }}
                    </span>
                  </div>
                </div>

                <!-- Reviews Section - Inline -->
                <div class="flex-1 border-t border-neutral-100 pt-3">
                  <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-neutral-700">Latest Reviews</h4>
                    <a
                      [routerLink]="['/locations', location.id]"
                      class="text-primary-600 hover:text-primary-700 text-xs font-medium"
                    >
                      All →
                    </a>
                  </div>

                  @if (loadingReviews()) {
                  <div class="space-y-2">
                    @for (i of [1,2]; track i) {
                    <div class="bg-neutral-50 rounded-lg p-2 animate-pulse">
                      <div class="h-3 bg-neutral-200 rounded w-1/2 mb-1"></div>
                      <div class="h-2 bg-neutral-200 rounded w-3/4"></div>
                    </div>
                    }
                  </div>
                  } @else { @if (locationReviews()[location.id] &&
                  locationReviews()[location.id].length > 0) {
                  <div class="space-y-2">
                    @for (review of locationReviews()[location.id]; track review.id) {
                    <a
                      [routerLink]="['/reviews', review.id]"
                      class="bg-neutral-50 rounded-lg p-2 hover:bg-neutral-100 transition-colors block"
                    >
                      <div class="flex items-center justify-between mb-1">
                        <div class="flex items-center space-x-2 flex-1 min-w-0">
                          <span class="font-medium text-neutral-900 text-sm truncate">{{
                            review.eventName
                          }}</span>
                          <div class="flex items-center space-x-1 flex-shrink-0">
                            <svg
                              class="w-3 h-3 text-yellow-500"
                              fill="currentColor"
                              viewBox="0 0 24 24"
                            >
                              <path
                                d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
                              ></path>
                            </svg>
                            <span class="font-semibold text-primary-700 text-xs">{{
                              review.rate.averageRating.toFixed(1)
                            }}</span>
                          </div>
                        </div>
                        <span class="text-xs text-neutral-400 flex-shrink-0 ml-2">{{
                          formatDate(review.createdAt)
                        }}</span>
                      </div>
                      @if (review.eventCount > 0) {
                      <div class="text-xs text-blue-600">
                        <i class="fas fa-repeat mr-1"></i>{{ review.eventCount }}x occurrences
                      </div>
                      }
                    </a>
                    }
                  </div>
                  } @else {
                  <div class="bg-neutral-50 rounded-lg p-3 text-center">
                    <p class="text-neutral-400 text-xs">No reviews</p>
                  </div>
                  } }
                </div>
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

      <!-- Footer -->
      <footer
        class="bg-gradient-to-br from-neutral-900 to-neutral-800 text-white relative overflow-hidden"
      >
        <!-- Decorative elements in footer -->
        <div
          class="absolute top-10 right-20 opacity-5 hidden lg:block"
          style="animation: float 9s ease-in-out infinite;"
        >
          <svg class="w-24 h-24 text-white" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M17,8C8,10 5.9,16.17 3.82,21.34L5.71,22L6.66,19.7C7.14,19.87 7.64,20 8,20C19,20 22,3 22,3C21,5 14,5.25 9,6.25C4,7.25 2,11.5 2,13.5C2,15.5 3.75,17.25 3.75,17.25C7,8 17,8 17,8Z"
            />
          </svg>
        </div>

        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 relative z-10">
          <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
            <!-- Brand -->
            <div class="col-span-1 md:col-span-2">
              <div class="flex items-center space-x-2 mb-3">
                <svg
                  class="w-6 h-6 text-primary-500"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M13 10V3L4 14h7v7l9-11h-7z"
                  ></path>
                </svg>
                <span class="text-xl font-bold">NewNow</span>
              </div>
              <p class="text-neutral-400 text-sm mb-3 max-w-md">
                Discover amazing events and venues in your city.
              </p>
              <div class="flex space-x-3">
                <a href="#" class="text-neutral-400 hover:text-primary-500 transition-colors">
                  <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path
                      d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"
                    />
                  </svg>
                </a>
                <a href="#" class="text-neutral-400 hover:text-primary-500 transition-colors">
                  <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path
                      d="M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z"
                    />
                  </svg>
                </a>
                <a href="#" class="text-neutral-400 hover:text-primary-500 transition-colors">
                  <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path
                      d="M12 0C8.74 0 8.333.015 7.053.072 5.775.132 4.905.333 4.14.63c-.789.306-1.459.717-2.126 1.384S.935 3.35.63 4.14C.333 4.905.131 5.775.072 7.053.012 8.333 0 8.74 0 12s.015 3.667.072 4.947c.06 1.277.261 2.148.558 2.913.306.788.717 1.459 1.384 2.126.667.666 1.336 1.079 2.126 1.384.766.296 1.636.499 2.913.558C8.333 23.988 8.74 24 12 24s3.667-.015 4.947-.072c1.277-.06 2.148-.262 2.913-.558.788-.306 1.459-.718 2.126-1.384.666-.667 1.079-1.335 1.384-2.126.296-.765.499-1.636.558-2.913.06-1.28.072-1.687.072-4.947s-.015-3.667-.072-4.947c-.06-1.277-.262-2.149-.558-2.913-.306-.789-.718-1.459-1.384-2.126C21.319 1.347 20.651.935 19.86.63c-.765-.297-1.636-.499-2.913-.558C15.667.012 15.26 0 12 0zm0 2.16c3.203 0 3.585.016 4.85.071 1.17.055 1.805.249 2.227.415.562.217.96.477 1.382.896.419.42.679.819.896 1.381.164.422.36 1.057.413 2.227.057 1.266.07 1.646.07 4.85s-.015 3.585-.074 4.85c-.061 1.17-.256 1.805-.421 2.227-.224.562-.479.96-.899 1.382-.419.419-.824.679-1.38.896-.42.164-1.065.36-2.235.413-1.274.057-1.649.07-4.859.07-3.211 0-3.586-.015-4.859-.074-1.171-.061-1.816-.256-2.236-.421-.569-.224-.96-.479-1.379-.899-.421-.419-.69-.824-.9-1.38-.165-.42-.359-1.065-.42-2.235-.045-1.26-.061-1.649-.061-4.844 0-3.196.016-3.586.061-4.861.061-1.17.255-1.814.42-2.234.21-.57.479-.96.9-1.381.419-.419.81-.689 1.379-.898.42-.166 1.051-.361 2.221-.421 1.275-.045 1.65-.06 4.859-.06l.045.03zm0 3.678c-3.405 0-6.162 2.76-6.162 6.162 0 3.405 2.76 6.162 6.162 6.162 3.405 0 6.162-2.76 6.162-6.162 0-3.405-2.76-6.162-6.162-6.162zM12 16c-2.21 0-4-1.79-4-4s1.79-4 4-4 4 1.79 4 4-1.79 4-4 4zm7.846-10.405c0 .795-.646 1.44-1.44 1.44-.795 0-1.44-.646-1.44-1.44 0-.794.646-1.439 1.44-1.439.793-.001 1.44.645 1.44 1.439z"
                    />
                  </svg>
                </a>
              </div>
            </div>

            <!-- Quick Links -->
            <div>
              <h3 class="text-base font-semibold mb-3">Quick Links</h3>
              <ul class="space-y-1.5">
                <li>
                  <a
                    routerLink="/events"
                    class="text-neutral-400 hover:text-primary-500 transition-colors text-sm"
                    >Browse Events</a
                  >
                </li>
                <li>
                  <a
                    routerLink="/locations"
                    class="text-neutral-400 hover:text-primary-500 transition-colors text-sm"
                    >Find Locations</a
                  >
                </li>
              </ul>
            </div>

            <!-- Support -->
            <div>
              <h3 class="text-base font-semibold mb-3">Support</h3>
              <ul class="space-y-1.5">
                <li>
                  <a
                    href="#"
                    class="text-neutral-400 hover:text-primary-500 transition-colors text-sm"
                    >Help Center</a
                  >
                </li>
                <li>
                  <a
                    href="#"
                    class="text-neutral-400 hover:text-primary-500 transition-colors text-sm"
                    >Privacy Policy</a
                  >
                </li>
              </ul>
            </div>
          </div>

          <!-- Bottom Bar -->
          <div
            class="border-t border-neutral-700 pt-4 flex flex-col md:flex-row justify-between items-center text-sm"
          >
            <p class="text-neutral-400 mb-2 md:mb-0">© 2025 NewNow. All rights reserved.</p>
            <div class="flex items-center space-x-2 text-neutral-400">
              <span>Made with</span>
              <svg class="w-4 h-4 text-red-500" fill="currentColor" viewBox="0 0 24 24">
                <path
                  d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                />
              </svg>
              <span>in Bijelo Polje</span>
            </div>
          </div>
        </div>
      </footer>
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

      @keyframes float {
        0%,
        100% {
          transform: translateY(0px) rotate(0deg);
        }
        50% {
          transform: translateY(-20px) rotate(5deg);
        }
      }
    `,
  ],
})
export class HomeComponent implements OnInit, AfterViewInit {
  @ViewChildren('videoElement') videoElements!: QueryList<ElementRef<HTMLVideoElement>>;

  todayEvents = signal<Event[]>([]);
  popularLocations = signal<Location[]>([]);
  locationReviews = signal<{ [locationId: number]: ReviewDTO[] }>({});
  loadingEvents = signal(true);
  loadingLocations = signal(true);
  loadingReviews = signal(true);

  // Video rotation state
  currentVideoIndex = signal(0);
  videos = [
    '/assets/videos/hero-1.mp4',
    '/assets/videos/hero-2.mp4',
    '/assets/videos/hero-3.mp4',
    '/assets/videos/hero-4.mp4',
  ];

  private videosLoaded = new Set<number>();
  private videosCanPlay = new Set<number>();

  constructor(private eventService: EventService, private locationService: LocationService) {}

  ngOnInit(): void {
    this.loadTodayEvents();
    this.loadPopularLocations();
  }

  ngAfterViewInit(): void {
    // Start rotation after videos are loaded
    setTimeout(() => this.startVideoRotation(), 1000);
  }

  onVideoLoaded(event: any, index: number): void {
    this.videosLoaded.add(index);
  }

  onVideoCanPlay(event: any, index: number): void {
    const video = event.target as HTMLVideoElement;
    this.videosCanPlay.add(index);

    // Play the first video immediately when it can play
    if (index === 0 && this.videosCanPlay.has(0)) {
      video.muted = true;
      video.play().catch((error) => {
        console.log('Video autoplay failed, trying again...', error);
        // Retry after a short delay
        setTimeout(() => {
          video.play().catch(() => {});
        }, 100);
      });
    }
  }

  startVideoRotation(): void {
    // Play current video
    this.playCurrentVideo();

    // Rotate to next video every 15 seconds with smooth fade
    setInterval(() => {
      const nextIndex = (this.currentVideoIndex() + 1) % this.videos.length;
      this.currentVideoIndex.set(nextIndex);
      this.playCurrentVideo();
    }, 15000);
  }

  private playCurrentVideo(): void {
    const videos = this.videoElements?.toArray();
    if (!videos) return;

    videos.forEach((videoRef, index) => {
      const video = videoRef.nativeElement;
      if (index === this.currentVideoIndex()) {
        video.currentTime = 0;
        video.muted = true;
        video.play().catch((error) => {
          console.log(`Failed to play video ${index}:`, error);
          // Retry after a short delay
          setTimeout(() => {
            video.play().catch(() => {});
          }, 100);
        });
      } else {
        video.pause();
      }
    });
  }

  imageSrc(url?: string): string {
    if (!url) return '/assets/placeholder.jpg';
    if (url.startsWith('http')) return url;
    // In development, prefix with backend URL
    const isDev = !window.location.origin.includes('production');
    return isDev ? `http://localhost:8080${url}` : url;
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
        // Load reviews for each location
        this.loadReviewsForLocations(locations);
      },
      error: () => {
        this.loadingLocations.set(false);
      },
    });
  }

  loadReviewsForLocations(locations: Location[]): void {
    if (locations.length === 0) {
      this.loadingReviews.set(false);
      return;
    }

    const reviewsMap: { [locationId: number]: ReviewDTO[] } = {};
    let loadedCount = 0;

    locations.forEach((location) => {
      this.locationService.getLocationReviews(location.id, 'date', 'desc', 0, 3).subscribe({
        next: (res) => {
          reviewsMap[location.id] = res.content || [];
          loadedCount++;
          if (loadedCount === locations.length) {
            this.locationReviews.set(reviewsMap);
            this.loadingReviews.set(false);
          }
        },
        error: () => {
          reviewsMap[location.id] = [];
          loadedCount++;
          if (loadedCount === locations.length) {
            this.locationReviews.set(reviewsMap);
            this.loadingReviews.set(false);
          }
        },
      });
    });
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

    return date.toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric' });
  }
}
