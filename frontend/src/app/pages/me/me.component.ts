import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../../services/user.service';
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

  constructor(private userService: UserService) {}

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
}
