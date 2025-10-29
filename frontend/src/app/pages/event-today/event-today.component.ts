import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event } from '../../models/event.model';

@Component({
  selector: 'app-event-today',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-3xl font-bold text-neutral-900">Today's Events</h1>
            <p class="text-neutral-600 mt-1">Handpicked happenings for your day</p>
          </div>
          <a routerLink="/events" class="btn-secondary">Browse all events</a>
        </div>

        <div *ngIf="loading()" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div *ngFor="let i of [1, 2, 3, 4, 5, 6]" class="card h-64 animate-pulse">
            <div class="h-40 bg-neutral-200"></div>
            <div class="p-4 space-y-2">
              <div class="h-4 bg-neutral-200 rounded"></div>
              <div class="h-4 bg-neutral-200 rounded w-1/2"></div>
            </div>
          </div>
        </div>

        <div *ngIf="!loading() && events().length === 0" class="card p-10 text-center">
          <p class="text-neutral-600">No events scheduled for today.</p>
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
              <img
                *ngIf="e.imageUrl"
                [src]="imageSrc(e.imageUrl)"
                class="w-full h-full object-cover"
                alt=""
              />
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
        </div>
      </div>
    </div>
  `,
})
export class EventTodayComponent implements OnInit {
  events = signal<Event[]>([]);
  loading = signal(true);

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.eventService.getTodayEvents().subscribe({
      next: (res) => {
        this.events.set(res || []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  imageSrc(url?: string): string {
    if (!url) return '/assets/placeholder.jpg';
    if (url.startsWith('http')) return url;
    return `http://localhost:8080${url}`;
  }
}
