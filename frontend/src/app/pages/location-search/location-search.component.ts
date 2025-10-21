import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SearchService } from '../../services/search.service';
import { LocationDTO } from '../../models/location.model';

@Component({
  selector: 'app-location-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-7xl mx-auto px-4">
        <h1 class="text-3xl font-bold text-neutral-900 mb-6">Find Locations</h1>

        <!-- Filters -->
        <div class="card p-4 mb-6 grid grid-cols-1 md:grid-cols-4 gap-4">
          <input class="input-field" type="text" [(ngModel)]="q" placeholder="Search by name" />
          <input class="input-field" type="text" [(ngModel)]="type" placeholder="Type" />
          <input class="input-field" type="text" [(ngModel)]="address" placeholder="Address" />
          <button class="btn-primary" (click)="apply()">Search</button>
        </div>

        <!-- Results -->
        <div *ngIf="loading()" class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div *ngFor="let _ of [1, 2, 3, 4, 5, 6, 7, 8, 9]" class="card h-48 animate-pulse"></div>
        </div>

        <div *ngIf="!loading() && items().length === 0" class="card p-8 text-center">
          <p class="text-neutral-600">No locations found.</p>
        </div>

        <div *ngIf="!loading() && items().length > 0" class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <a
            *ngFor="let l of items()"
            class="card group overflow-hidden hover:-translate-y-1 transition"
            [routerLink]="['/locations', l.id]"
          >
            <div class="h-40 bg-neutral-200">
              <img *ngIf="l.imageUrl" [src]="l.imageUrl" class="w-full h-full object-cover" />
            </div>
            <div class="p-4">
              <h3 class="font-semibold text-lg">{{ l.name }}</h3>
              <div class="text-sm text-neutral-500">{{ l.type }}</div>
              <div class="text-sm text-neutral-500">{{ l.address }}</div>
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
export class LocationSearchComponent implements OnInit {
  q = '';
  type = '';
  address = '';

  items = signal<LocationDTO[]>([]);
  loading = signal(true);
  page = 0;
  size = 12;
  totalPages = 1;

  constructor(private search: SearchService) {}

  ngOnInit(): void {
    this.apply();
  }

  apply() {
    this.loading.set(true);
    this.search
      .searchLocations({
        q: this.q || undefined,
        type: this.type || undefined,
        address: this.address || undefined,
        page: this.page,
        size: this.size,
      })
      .subscribe({
        next: (res) => {
          this.items.set(res.content || []);
          this.totalPages = res.totalPages || 1;
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  prev() {
    if (this.page > 0) {
      this.page--;
      this.apply();
    }
  }
  next() {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.apply();
    }
  }
}
