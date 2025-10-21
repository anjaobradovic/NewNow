import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <nav
      class="bg-white/80 backdrop-blur-md shadow-sm fixed top-0 left-0 right-0 z-50 border-b border-neutral-100"
    >
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo -->
          <div class="flex items-center">
            <a routerLink="/" class="flex items-center space-x-3 group">
              <!-- Simple clean logo -->
              <div
                class="w-10 h-10 bg-gradient-to-br from-primary-500 to-primary-700 rounded-xl flex items-center justify-center transform group-hover:scale-105 transition-transform shadow-sm"
              >
                <svg
                  class="w-6 h-6 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M13 10V3L4 14h7v7l9-11h-7z"
                  />
                </svg>
              </div>
              <span class="text-xl font-bold text-neutral-900">NewNow</span>
            </a>
          </div>

          <!-- Mobile toggle -->
          <button
            class="md:hidden inline-flex items-center justify-center w-10 h-10 rounded-xl hover:bg-neutral-100 text-neutral-700"
            (click)="toggleMobile()"
            aria-label="Toggle navigation"
          >
            <svg
              *ngIf="!openMobile()"
              class="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 6h16M4 12h16M4 18h16"
              />
            </svg>
            <svg
              *ngIf="openMobile()"
              class="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>

          <!-- Desktop Navigation -->
          <div class="hidden md:flex items-center space-x-1">
            @if (authService.isAuthenticated()) {
            <a
              routerLink="/"
              class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium"
            >
              Discover
            </a>

            <!-- Events dropdown -->
            <div
              class="relative group before:content-[''] before:absolute before:left-0 before:top-full before:h-2 before:w-full"
            >
              <a
                routerLink="/events"
                class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium inline-flex items-center gap-1"
              >
                Events
                <svg
                  class="w-4 h-4 transition-transform group-hover:rotate-180"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </a>
              <!-- submenu -->
              <div
                class="absolute left-0 top-full z-50 w-56 bg-white border border-neutral-100 rounded-xl shadow-lg py-2 invisible opacity-0 group-hover:visible group-hover:opacity-100 translate-y-1 group-hover:translate-y-0 transition ease-out duration-150"
              >
                <a
                  routerLink="/events/today"
                  class="block px-4 py-2 text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
                  >Today's Events</a
                >
                <a
                  routerLink="/events"
                  class="block px-4 py-2 text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
                  >Browse Events</a
                >
              </div>
            </div>

            <a
              routerLink="/locations"
              class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium"
            >
              Locations
            </a>
            <a
              routerLink="/search/locations"
              class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium"
            >
              Find Locations
            </a>
            <a
              routerLink="/me"
              class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium"
            >
              My Profile
            </a>
            <!-- Feed dropdown -->
            <div
              class="relative group before:content-[''] before:absolute before:left-0 before:top-full before:h-2 before:w-full"
            >
              <button
                class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium inline-flex items-center gap-1"
              >
                Discover
                <svg
                  class="w-4 h-4 transition-transform group-hover:rotate-180"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </button>
              <div
                class="absolute left-0 top-full z-50 w-56 bg-white border border-neutral-100 rounded-xl shadow-lg py-2 invisible opacity-0 group-hover:visible group-hover:opacity-100 translate-y-1 group-hover:translate-y-0 transition ease-out duration-150"
              >
                <a
                  routerLink="/feed/popular-locations"
                  class="block px-4 py-2 text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
                  >Popular Venues</a
                >
                <a
                  routerLink="/feed/popular-location-latest-reviews"
                  class="block px-4 py-2 text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
                  >Latest Reviews</a
                >
              </div>
            </div>
            @if (isAdmin()) {
            <div
              class="relative group before:content-[''] before:absolute before:left-0 before:top-full before:h-2 before:w-full"
            >
              <a
                routerLink="/admin"
                class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium inline-flex items-center gap-1"
              >
                Admin
                <svg
                  class="w-4 h-4 transition-transform group-hover:rotate-180"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </a>
              <div
                class="absolute left-0 top-full z-50 w-56 bg-white border border-neutral-100 rounded-xl shadow-lg py-2 invisible opacity-0 group-hover:visible group-hover:opacity-100 translate-y-1 group-hover:translate-y-0 transition ease-out duration-150"
              >
                <a
                  routerLink="/admin"
                  class="block px-4 py-2 text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
                  >Dashboard</a
                >
                <a
                  routerLink="/admin/requests"
                  class="block px-4 py-2 text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
                  >Requests</a
                >
              </div>
            </div>
            } }
          </div>

          <!-- Auth Buttons (desktop) -->
          <div class="hidden md:flex items-center space-x-3">
            @if (authService.isAuthenticated()) {
            <a routerLink="/me" class="flex items-center gap-2 group">
              <div
                class="w-8 h-8 rounded-xl bg-primary-100 text-primary-700 flex items-center justify-center font-bold"
              >
                {{ initials() }}
              </div>
              <span class="text-sm text-neutral-700 group-hover:text-primary-600">{{
                authService.currentUser()?.name
              }}</span>
            </a>
            <button
              (click)="logout()"
              class="px-4 py-2 text-sm font-medium text-neutral-600 hover:text-primary-600 hover:bg-neutral-100 rounded-xl transition-all duration-200"
            >
              Sign Out
            </button>
            } @else {
            <a
              routerLink="/auth/login"
              class="px-4 py-2 text-sm font-medium text-neutral-700 hover:text-primary-600 hover:bg-neutral-100 rounded-xl transition-all duration-200"
            >
              Sign In
            </a>
            <a routerLink="/auth/register-request" class="btn-primary text-sm"> Get Started </a>
            }
          </div>
        </div>
      </div>

      <!-- Mobile Menu -->
      @if (openMobile()) {
      <div class="md:hidden border-t border-neutral-100 bg-white">
        <div class="px-4 py-3 space-y-1">
          @if (authService.isAuthenticated()) {
          <a
            routerLink="/"
            class="block px-3 py-2 rounded-lg text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
            >Discover</a
          >
          <button
            class="w-full flex items-center justify-between px-3 py-2 rounded-lg text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
            (click)="toggleEventsMobile()"
          >
            <span>Events</span>
            <svg
              class="w-4 h-4 transition-transform"
              [class.rotate-180]="openEventsMobile()"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 9l-7 7-7-7"
              />
            </svg>
          </button>
          @if (openEventsMobile()) {
          <div class="ml-3">
            <a
              routerLink="/events/today"
              class="block px-3 py-2 rounded-lg text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
              >Today's Events</a
            >
            <a
              routerLink="/events"
              class="block px-3 py-2 rounded-lg text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
              >Browse Events</a
            >
          </div>
          }
          <a
            routerLink="/locations"
            class="block px-3 py-2 rounded-lg text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
            >Locations</a
          >
          <a
            routerLink="/search/locations"
            class="block px-3 py-2 rounded-lg text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
            >Find Locations</a
          >
          <a
            routerLink="/me"
            class="block px-3 py-2 rounded-lg text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
            >My Profile</a
          >
          <button
            class="w-full flex items-center justify-between px-3 py-2 rounded-lg text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
            (click)="toggleDiscoverMobile()"
          >
            <span>Discover</span>
            <svg
              class="w-4 h-4 transition-transform"
              [class.rotate-180]="openDiscoverMobile()"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 9l-7 7-7-7"
              />
            </svg>
          </button>
          @if (openDiscoverMobile()) {
          <div class="ml-3">
            <a
              routerLink="/feed/popular-locations"
              class="block px-3 py-2 rounded-lg text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
              >Popular Venues</a
            >
            <a
              routerLink="/feed/popular-location-latest-reviews"
              class="block px-3 py-2 rounded-lg text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
              >Latest Reviews</a
            >
          </div>
          } @if (isAdmin()) {
          <button
            class="w-full flex items-center justify-between px-3 py-2 rounded-lg text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
            (click)="toggleAdminMobile()"
          >
            <span>Admin</span>
            <svg
              class="w-4 h-4 transition-transform"
              [class.rotate-180]="openAdminMobile()"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 9l-7 7-7-7"
              />
            </svg>
          </button>
          @if (openAdminMobile()) {
          <div class="ml-3">
            <a
              routerLink="/admin"
              class="block px-3 py-2 rounded-lg text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
              >Dashboard</a
            >
            <a
              routerLink="/admin/requests"
              class="block px-3 py-2 rounded-lg text-sm text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
              >Requests</a
            >
          </div>
          } }
          <div class="pt-3 border-t border-neutral-100">
            @if (authService.isAuthenticated()) {
            <button
              (click)="logout()"
              class="w-full text-left px-3 py-2 rounded-lg text-neutral-700 hover:bg-neutral-100"
            >
              Sign Out
            </button>
            } @else {
            <a
              routerLink="/auth/login"
              class="block px-3 py-2 rounded-lg text-neutral-700 hover:bg-neutral-100"
              >Sign In</a
            >
            <a
              routerLink="/auth/register-request"
              class="block mt-1 px-3 py-2 rounded-lg bg-primary-600 text-white text-center"
              >Get Started</a
            >
            }
          </div>
          } @else {
          <!-- Show minimal menu for guests -->
          <a
            routerLink="/events/today"
            class="block px-3 py-2 rounded-lg text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
            >Today's Events</a
          >
          <a
            routerLink="/events"
            class="block px-3 py-2 rounded-lg text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
            >Browse Events</a
          >
          <a
            routerLink="/locations"
            class="block px-3 py-2 rounded-lg text-neutral-700 hover:bg-primary-50 hover:text-primary-700"
            >Locations</a
          >
          <div class="pt-3 border-t border-neutral-100">
            <a
              routerLink="/auth/login"
              class="block px-3 py-2 rounded-lg text-neutral-700 hover:bg-neutral-100"
              >Sign In</a
            >
            <a
              routerLink="/auth/register-request"
              class="block mt-1 px-3 py-2 rounded-lg bg-primary-600 text-white text-center"
              >Get Started</a
            >
          </div>
          }
        </div>
      </div>
      }
    </nav>
    <!-- Spacer to prevent content from being hidden under fixed navbar -->
    <div class="h-16"></div>
  `,
  styles: [],
})
export class NavbarComponent {
  openMobile = signal(false);
  openEventsMobile = signal(false);
  openDiscoverMobile = signal(false);
  openAdminMobile = signal(false);

  constructor(public authService: AuthService, private router: Router) {}

  toggleMobile() {
    this.openMobile.update((v) => !v);
  }
  toggleEventsMobile() {
    this.openEventsMobile.update((v) => !v);
  }
  toggleDiscoverMobile() {
    this.openDiscoverMobile.update((v) => !v);
  }
  toggleAdminMobile() {
    this.openAdminMobile.update((v) => !v);
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/']),
      error: () => {
        this.authService.clearSession();
        this.router.navigate(['/']);
      },
    });
  }

  isAdmin(): boolean {
    const user = this.authService.currentUser();
    return user?.roles?.includes('ROLE_ADMIN') || false;
  }

  initials(): string {
    const n = this.authService.currentUser()?.name || this.authService.currentUser()?.email || '';
    return n
      .split(' ')
      .map((s) => s[0])
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }
}
