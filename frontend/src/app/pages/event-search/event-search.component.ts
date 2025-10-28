import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Event } from '../../models/event.model';
import { SearchService } from '../../services/search.service';

@Component({
  selector: 'app-event-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-7xl mx-auto px-4">
        <h1 class="text-3xl font-bold text-neutral-900 mb-6">Explore Events</h1>

        <!-- Filters -->
        <div class="card p-4 mb-6 grid grid-cols-1 md:grid-cols-5 gap-4">
          <input type="text" class="input-field" [(ngModel)]="address" placeholder="Address" />
          <input type="text" class="input-field" [(ngModel)]="type" placeholder="Type" />
          <input type="date" class="input-field" [(ngModel)]="startDate" />
          <input type="date" class="input-field" [(ngModel)]="endDate" />
          <div class="flex items-center gap-3">
            <input
              type="number"
              min="0"
              class="input-field"
              placeholder="Min"
              [(ngModel)]="minPrice"
            />
            <input
              type="number"
              min="0"
              class="input-field"
              placeholder="Max"
              [(ngModel)]="maxPrice"
            />
          </div>
          <label class="inline-flex items-center gap-2 text-sm text-neutral-700">
            <input type="checkbox" [(ngModel)]="past" /> Past
          </label>
          <label class="inline-flex items-center gap-2 text-sm text-neutral-700">
            <input type="checkbox" [(ngModel)]="future" /> Future
          </label>
          <label class="inline-flex items-center gap-2 text-sm text-neutral-700">
            <input type="checkbox" [(ngModel)]="regularOnly" /> Regular events only
          </label>
          <button class="btn-primary w-full md:w-auto" (click)="apply()">Search</button>
        </div>

        <!-- Results -->
        <div *ngIf="loading()" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div *ngFor="let i of [1, 2, 3, 4, 5, 6, 7, 8, 9]" class="card h-64 animate-pulse"></div>
        </div>

        <div *ngIf="!loading() && events().length === 0" class="card p-10 text-center">
          <p class="text-neutral-600">No events found with selected filters.</p>
        </div>

        <div
          *ngIf="!loading() && events().length > 0"
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"
        >
          <div
            *ngFor="let e of events()"
            class="card group overflow-hidden hover:-translate-y-1 transition transform"
          >
            <a [routerLink]="['/events', e.id]" class="block">
              <div class="h-44 bg-neutral-200">
                <img *ngIf="e.imageUrl" [src]="e.imageUrl" class="w-full h-full object-cover" />
              </div>
              <div class="p-5">
                <div class="flex items-center justify-between">
                  <h3 class="font-semibold text-lg">{{ e.name }}</h3>
                  <span class="text-sm text-primary-700">{{
                    e.price ? e.price + ' RSD' : 'Free'
                  }}</span>
                </div>
                <div class="text-sm text-neutral-500 mt-1">{{ e.type }} • {{ e.date }}</div>
                <div class="text-sm text-neutral-500">{{ e.locationName }}</div>
                <div class="mt-2">
                  <span
                    class="inline-block px-2 py-1 text-xs rounded-full"
                    [class.bg-primary-100]="e.recurrent"
                    [class.text-primary-700]="e.recurrent"
                    [class.bg-neutral-100]="!e.recurrent"
                    [class.text-neutral-600]="!e.recurrent"
                  >
                    {{ e.recurrent ? 'Regular event' : 'One-time' }}
                  </span>
                </div>
              </div>
            </a>
            <div *ngIf="isManagerOrAdmin" class="px-5 pb-4">
              <a
                [routerLink]="['/events', e.id, 'edit']"
                class="inline-block w-full text-center px-4 py-2 bg-primary-100 hover:bg-primary-200 text-primary-700 rounded-lg text-sm font-medium transition-colors"
              >
                Edit Event
              </a>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div
          *ngIf="!loading() && totalPages > 1"
          class="flex items-center justify-center gap-4 mt-8"
        >
          <button class="btn-secondary" (click)="prev()" [disabled]="page === 0">Prev</button>
          <div class="text-sm text-neutral-600">Page {{ page + 1 }} / {{ totalPages }}</div>
          <button class="btn-primary" (click)="next()" [disabled]="page + 1 >= totalPages">
            Next
          </button>
        </div>
      </div>
    </div>
  `,
})
export class EventSearchComponent implements OnInit {
  // filters
  type = '';
  address = '';
  startDate = '';
  endDate = '';
  minPrice?: number;
  maxPrice?: number;
  past?: boolean;
  future?: boolean;
  regularOnly?: boolean;

  // data
  events = signal<Event[]>([]);
  loading = signal(true);
  page = 0;
  size = 9;
  totalPages = 1;

  // permissions
  isManagerOrAdmin = false;

  constructor(private search: SearchService) {}

  ngOnInit(): void {
    this.checkPermissions();
    this.apply();
  }

  checkPermissions(): void {
    try {
      const user = JSON.parse(localStorage.getItem('user_data') || 'null');
      this.isManagerOrAdmin =
        !!user?.roles?.includes('ROLE_ADMIN') || !!user?.roles?.includes('ROLE_MANAGER');
    } catch {}
  }

  apply(): void {
    this.loading.set(true);
    this.search
      .searchEvents({
        type: this.type || undefined,
        address: this.address || undefined,
        startDate: this.startDate || undefined,
        endDate: this.endDate || undefined,
        minPrice: this.minPrice,
        maxPrice: this.maxPrice,
        past: this.past,
        future: this.future,
        regularOnly: this.regularOnly,
        page: this.page,
        size: this.size,
      })
      .subscribe({
        next: (res) => {
          const content = Array.isArray(res?.content) ? res.content : (res as any) || [];
          this.events.set(content);
          this.totalPages = res?.totalPages ?? 1;
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  prev(): void {
    if (this.page > 0) {
      this.page--;
      this.apply();
    }
  }
  next(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.apply();
    }
  }
}
