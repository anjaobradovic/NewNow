import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LocationService } from '../../services/location.service';
import { LocationDTO } from '../../models/location.model';

@Component({
  selector: 'app-location-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h1 class="text-3xl font-bold text-neutral-900">Locations</h1>
            <p class="text-neutral-600 mt-1">Explore venues across the city</p>
          </div>
          <a routerLink="/locations/new" class="btn-primary" *ngIf="isAdmin">New location</a>
        </div>

        <div class="card p-4 mb-6">
          <div class="flex flex-wrap gap-3 items-center">
            <input
              class="input-field w-full md:w-80"
              placeholder="Search by name, address or type"
              [(ngModel)]="search"
              (ngModelChange)="onSearchChange()"
            />
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <a
            class="card hover:bg-primary-50 group"
            *ngFor="let loc of locations()"
            [routerLink]="['/locations', loc.id]"
          >
            <div class="h-40 w-full bg-neutral-100 overflow-hidden">
              <img
                *ngIf="loc.imageUrl"
                [src]="imageSrc(loc.imageUrl)"
                class="w-full h-full object-cover group-hover:scale-105 transition"
              />
            </div>
            <div class="p-4">
              <div class="flex items-start justify-between">
                <div>
                  <h3 class="text-lg font-semibold text-neutral-900">{{ loc.name }}</h3>
                  <p class="text-neutral-600 text-sm line-clamp-1">{{ loc.address }}</p>
                </div>
                <span class="px-2 py-1 text-xs rounded-full bg-primary-100 text-primary-700">{{
                  loc.type
                }}</span>
              </div>
              <div class="mt-3 text-sm text-neutral-500">Rating: {{ loc.totalRating || 0 }}</div>
            </div>
          </a>
        </div>

        <div class="flex items-center justify-center gap-3 mt-8">
          <button class="btn-secondary" (click)="prev()" [disabled]="page === 0">Back</button>
          <div class="text-sm text-neutral-600">Page {{ page + 1 }} of {{ totalPages }}</div>
          <button class="btn-primary" (click)="next()" [disabled]="page + 1 >= totalPages">
            Next
          </button>
        </div>
      </section>
    </div>
  `,
})
export class LocationListComponent implements OnInit {
  locations = signal<LocationDTO[]>([]);
  search = '';
  page = 0;
  size = 12;
  totalPages = 1;
  isAdmin = false;

  constructor(private locationService: LocationService) {}

  ngOnInit(): void {
    // Basic role check via localStorage
    try {
      const user = JSON.parse(localStorage.getItem('user_data') || 'null');
      this.isAdmin = !!user?.roles?.includes('ROLE_ADMIN');
    } catch {}
    this.load();
  }

  load(): void {
    this.locationService.getLocations(this.search, this.page, this.size).subscribe({
      next: (res) => {
        this.locations.set(res.locations || []);
        this.totalPages = res.totalPages || 1;
      },
    });
  }

  onSearchChange(): void {
    this.page = 0;
    this.load();
  }

  prev(): void {
    if (this.page > 0) {
      this.page--;
      this.load();
    }
  }

  next(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.load();
    }
  }

  imageSrc(url?: string): string | undefined {
    if (!url) return undefined;
    const isDev = typeof window !== 'undefined' && window.location.port === '4200';
    if (isDev && url.startsWith('/uploads/')) {
      return `http://localhost:8080${url}`;
    }
    return url;
  }
}
