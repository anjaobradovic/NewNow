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

          <!-- Desktop Navigation -->
          <div class="hidden md:flex items-center space-x-1">
            @if (authService.isAuthenticated()) {
            <a
              routerLink="/"
              class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium"
            >
              Discover
            </a>
            <a
              routerLink="/events"
              class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium"
            >
              Events
            </a>
            <a
              routerLink="/locations"
              class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium"
            >
              Locations
            </a>
            <a
              routerLink="/me"
              class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium"
            >
              My Profile
            </a>
            @if (isAdmin()) {
            <a
              routerLink="/admin"
              class="px-4 py-2 text-neutral-700 hover:text-primary-600 hover:bg-primary-50 rounded-xl transition-all duration-200 font-medium"
            >
              Admin Dashboard
            </a>
            } }
          </div>

          <!-- Auth Buttons -->
          <div class="flex items-center space-x-3">
            @if (authService.isAuthenticated()) {
            <div class="flex items-center space-x-3">
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
            </div>
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
    </nav>
    <!-- Spacer to prevent content from being hidden under fixed navbar -->
    <div class="h-16"></div>
  `,
  styles: [],
})
export class NavbarComponent {
  constructor(public authService: AuthService, private router: Router) {}

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
