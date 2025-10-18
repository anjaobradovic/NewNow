import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <nav class="bg-white shadow-sm sticky top-0 z-50 border-b border-neutral-100">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo -->
          <div class="flex items-center space-x-2">
            <a routerLink="/" class="flex items-center space-x-3 group">
              <div
                class="w-10 h-10 bg-gradient-to-br from-primary-500 to-primary-700 rounded-xl flex items-center justify-center transform group-hover:scale-105 transition-transform"
              >
                <span class="text-white font-bold text-xl">N</span>
              </div>
              <span class="text-xl font-bold text-neutral-900">NewNow</span>
            </a>
          </div>

          <!-- Desktop Navigation -->
          <div class="hidden md:flex items-center space-x-6">
            @if (authService.isAuthenticated()) {
            <a
              routerLink="/"
              class="text-neutral-700 hover:text-primary-600 transition-colors font-medium"
            >
              Discover
            </a>
            <a
              routerLink="/events"
              class="text-neutral-700 hover:text-primary-600 transition-colors font-medium"
            >
              Events
            </a>
            <a
              routerLink="/locations"
              class="text-neutral-700 hover:text-primary-600 transition-colors font-medium"
            >
              Locations
            </a>
            }
          </div>

          <!-- Auth Buttons -->
          <div class="flex items-center space-x-4">
            @if (authService.isAuthenticated()) {
            <div class="flex items-center space-x-3">
              <span class="text-sm text-neutral-600">{{ authService.currentUser()?.name }}</span>
              <button
                (click)="logout()"
                class="px-4 py-2 text-sm font-medium text-neutral-700 hover:text-primary-600 transition-colors"
              >
                Sign Out
              </button>
            </div>
            } @else {
            <a
              routerLink="/login"
              class="px-4 py-2 text-sm font-medium text-neutral-700 hover:text-primary-600 transition-colors"
            >
              Sign In
            </a>
            <a routerLink="/register" class="btn-primary text-sm"> Get Started </a>
            }
          </div>
        </div>
      </div>
    </nav>
  `,
  styles: [],
})
export class NavbarComponent {
  constructor(public authService: AuthService, private router: Router) {}

  logout(): void {
    this.authService.logout();
  }
}
