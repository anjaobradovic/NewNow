import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event } from '../../models/event.model';

@Component({
  selector: 'app-event-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-7xl mx-auto px-4">
        <h1 class="text-3xl font-bold text-neutral-900 mb-6">Explore Events</h1>

        <!-- Filters -->
        <div class="card p-4 mb-6 grid grid-cols-1 md:grid-cols-4 gap-4">
          <input type="text" class="input-field" [(ngModel)]="address" placeholder="Address" />
          <input type="text" class="input-field" [(ngModel)]="type" placeholder="Type" />
          <input type="date" class="input-field" [(ngModel)]="date" />
          <div class="flex items-center gap-3">
            <input
              type="number"
              min="0"
              class="input-field"
              placeholder="Min price"
              [(ngModel)]="priceMin"
            />
            <input
              type="number"
              min="0"
              class="input-field"
              placeholder="Max price"
              [(ngModel)]="priceMax"
            />
          </div>
          <label class="inline-flex items-center gap-2 text-sm text-neutral-700">
            <input type="checkbox" [(ngModel)]="isFree" /> Free only
          </label>
          <label class="inline-flex items-center gap-2 text-sm text-neutral-700">
            <input type="checkbox" [(ngModel)]="isRegular" /> Regular
          </label>
          <button class="btn-primary w-full md:w-auto" (click)="apply()">Search</button>
        </div>

        <!-- Results -->
        <div *ngIf="loading()" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div *ngFor="let i of [1, 2, 3, 4, 5, 6, 7, 8, 9]" class="card h-64 animate-pulse">
            <div class="h-40 bg-neutral-200"></div>
            <div class="p-4 space-y-2">
              <div class="h-4 bg-neutral-200 rounded"></div>
              <div class="h-4 bg-neutral-200 rounded w-1/2"></div>
            </div>
          </div>
        </div>

        <div *ngIf="!loading() && events().length === 0" class="card p-10 text-center">
          <p class="text-neutral-600">No events found with selected filters.</p>
        </div>

        <div
          *ngIf="!loading() && events().length > 0"
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"
        >
          <a
            *ngFor="let e of events()"
            class="card group overflow-hidden hover:-translate-y-1 transition transform"
            [routerLink]="['/events', e.id]"
          >
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
            </div>
          </a>
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
  date = '';
  priceMin?: number;
  priceMax?: number;
  isFree?: boolean;
  isRegular?: boolean;

  // data
  events = signal<Event[]>([]);
  loading = signal(true);
  page = 0;
  size = 9;
  totalPages = 1;

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.apply();
  }

  apply(): void {
    this.loading.set(true);
    this.eventService
      .searchEvents({
        type: this.type || undefined,
        address: this.address || undefined,
        date: this.date || undefined,
        priceMin: this.priceMin,
        priceMax: this.priceMax,
        isFree: this.isFree,
        isRegular: this.isRegular,
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
