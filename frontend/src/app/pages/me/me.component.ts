import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { UserProfile } from '../../models/user.model';

@Component({
  selector: 'app-me',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <section class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-4xl font-bold text-neutral-900">My Profile</h1>
            <p class="text-neutral-600 mt-1">Account overview and personal details</p>
          </div>
          <div class="flex gap-3">
            <a routerLink="/me/edit" class="btn-secondary">Edit profile</a>
            <a routerLink="/me/change-password" class="btn-primary">Change password</a>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="card p-6">
            <div class="flex flex-col items-center text-center">
              <div class="w-28 h-28 rounded-2xl overflow-hidden bg-primary-100 mb-4">
                <img
                  *ngIf="profile()?.avatarUrl"
                  [src]="avatarSrc()"
                  class="w-full h-full object-cover"
                  (error)="onImageError()"
                />
                <div
                  *ngIf="!profile()?.avatarUrl"
                  class="w-full h-full flex items-center justify-center text-primary-600 font-bold text-xl"
                >
                  {{ initials() }}
                </div>
              </div>
              <h2 class="text-2xl font-bold text-neutral-900">{{ profile()?.name }}</h2>
              <p class="text-neutral-600">{{ profile()?.email }}</p>
              <div class="mt-4 flex flex-wrap gap-2 justify-center">
                <span
                  *ngFor="let r of profile()?.roles || []"
                  class="px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-xs font-semibold"
                  >{{ r.replace('ROLE_', '') }}</span
                >
              </div>
            </div>
          </div>

          <div class="md:col-span-2 space-y-6">
            <div class="card p-6">
              <h3 class="text-lg font-semibold mb-4">Basic information</h3>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <p class="text-sm text-neutral-500">Phone</p>
                  <p class="font-medium">{{ profile()?.phoneNumber || '—' }}</p>
                </div>
                <div>
                  <p class="text-sm text-neutral-500">Birthday</p>
                  <p class="font-medium">{{ profile()?.birthday || '—' }}</p>
                </div>
                <div>
                  <p class="text-sm text-neutral-500">Address</p>
                  <p class="font-medium">{{ profile()?.address || '—' }}</p>
                </div>
                <div>
                  <p class="text-sm text-neutral-500">City</p>
                  <p class="font-medium">{{ profile()?.city || '—' }}</p>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <a routerLink="/me/reviews" class="card p-6 hover:bg-primary-50 group">
                <div class="flex items-center justify-between">
                  <div>
                    <h4 class="text-lg font-semibold">My reviews</h4>
                    <p class="text-neutral-600">Browse and manage your reviews</p>
                  </div>
                  <div
                    class="w-10 h-10 rounded-xl bg-primary-100 text-primary-700 flex items-center justify-center group-hover:scale-110 transition"
                  >
                    📝
                  </div>
                </div>
              </a>
              <a routerLink="/me/managed-locations" class="card p-6 hover:bg-primary-50 group">
                <div class="flex items-center justify-between">
                  <div>
                    <h4 class="text-lg font-semibold">Managed locations</h4>
                    <p class="text-neutral-600">See the list and status</p>
                  </div>
                  <div
                    class="w-10 h-10 rounded-xl bg-primary-100 text-primary-700 flex items-center justify-center group-hover:scale-110 transition"
                  >
                    📍
                  </div>
                </div>
              </a>
            </div>

            <!-- Manager Panel -->
            <div *ngIf="isManager()" class="card p-6 bg-gradient-to-br from-blue-50 to-purple-50">
              <div class="flex items-center gap-3 mb-4">
                <div class="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center">
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
                      d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                    />
                  </svg>
                </div>
                <div>
                  <h4 class="text-lg font-semibold text-neutral-900">Manager Panel</h4>
                  <p class="text-sm text-neutral-600">
                    Manage your locations, events, and moderate reviews
                  </p>
                </div>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                <a
                  routerLink="/me/managed-locations"
                  class="flex items-center gap-3 p-3 bg-white rounded-lg hover:bg-blue-50 transition-all group"
                >
                  <div
                    class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center group-hover:scale-110 transition"
                  >
                    🏢
                  </div>
                  <div class="flex-1">
                    <div class="font-medium text-neutral-900">My Locations</div>
                    <div class="text-xs text-neutral-600">View & manage locations</div>
                  </div>
                  <svg
                    class="w-5 h-5 text-neutral-400 group-hover:text-blue-600 group-hover:translate-x-1 transition"
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
                </a>
                <a
                  routerLink="/manager/reviews"
                  class="flex items-center gap-3 p-3 bg-white rounded-lg hover:bg-purple-50 transition-all group"
                >
                  <div
                    class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center group-hover:scale-110 transition"
                  >
                    🛡️
                  </div>
                  <div class="flex-1">
                    <div class="font-medium text-neutral-900">Moderate Reviews</div>
                    <div class="text-xs text-neutral-600">Hide or delete reviews</div>
                  </div>
                  <svg
                    class="w-5 h-5 text-neutral-400 group-hover:text-purple-600 group-hover:translate-x-1 transition"
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
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  `,
  styles: [],
})
export class MeComponent implements OnInit {
  profile = signal<UserProfile | null>(null);
  loading = signal(true);
  private cacheBuster = signal<string>('');

  constructor(private userService: UserService, private authService: AuthService) {}

  ngOnInit(): void {
    this.cacheBuster.set(String(Date.now()));
    this.userService.getMe().subscribe({
      next: (p) => {
        this.profile.set(p);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  avatarSrc(): string | undefined {
    const url = this.profile()?.avatarUrl;
    if (!url) return undefined;
    const isDev = typeof window !== 'undefined' && window.location.port === '4200';
    if (isDev && url.startsWith('/uploads/')) {
      return `http://localhost:8080${url}?v=${this.cacheBuster()}`;
    }
    return `${url}?v=${this.cacheBuster()}`;
  }

  initials(): string {
    const n = this.profile()?.name || this.profile()?.email || '';
    return n
      .split(' ')
      .map((s) => s[0])
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }

  onImageError(): void {
    const p = this.profile();
    if (p) this.profile.set({ ...p, avatarUrl: undefined });
  }

  isManager(): boolean {
    return this.authService.hasRole('ROLE_MANAGER') || this.authService.hasRole('ROLE_ADMIN');
  }
}
