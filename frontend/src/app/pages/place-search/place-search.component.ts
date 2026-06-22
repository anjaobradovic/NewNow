import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import {
  PlaceSearchService,
  PlaceSearchResult,
} from '../../services/place-search.service';

@Component({
  selector: 'app-place-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarComponent],
  template: `
    <app-navbar />
    <div class="min-h-screen bg-gradient-to-br from-neutral-50 to-primary-50/30">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h1 class="text-3xl font-bold text-neutral-900 mb-2">Place full-text search</h1>
        <p class="text-neutral-600 mb-8">
          Powered by Elasticsearch. Name, UI description, and uploaded PDF description are
          all searched independently; review-count range filters the results.
        </p>

        <form
          class="bg-white rounded-2xl shadow-sm border border-neutral-100 p-6 mb-8 grid grid-cols-1 md:grid-cols-2 gap-4"
          (ngSubmit)="onSearch()"
        >
          <label class="block">
            <span class="text-sm text-neutral-600">Name contains</span>
            <input class="input-field" [(ngModel)]="filters.name" name="name" placeholder="e.g. fest" />
          </label>
          <label class="block">
            <span class="text-sm text-neutral-600">Description contains</span>
            <input
              class="input-field"
              [(ngModel)]="filters.description"
              name="description"
              placeholder="e.g. terrace"
            />
          </label>
          <label class="block">
            <span class="text-sm text-neutral-600">PDF text contains</span>
            <input
              class="input-field"
              [(ngModel)]="filters.pdf"
              name="pdf"
              placeholder="searches uploaded PDF"
            />
          </label>
          <div class="grid grid-cols-2 gap-3">
            <label class="block">
              <span class="text-sm text-neutral-600">Reviews from</span>
              <input
                type="number"
                min="0"
                class="input-field"
                [(ngModel)]="filters.reviewsFrom"
                name="reviewsFrom"
              />
            </label>
            <label class="block">
              <span class="text-sm text-neutral-600">Reviews to</span>
              <input
                type="number"
                min="0"
                class="input-field"
                [(ngModel)]="filters.reviewsTo"
                name="reviewsTo"
              />
            </label>
          </div>
          <div class="md:col-span-2 flex items-center gap-3">
            <button type="submit" class="btn-primary" [disabled]="loading">Search</button>
            <button type="button" class="btn-secondary" (click)="clear()">Clear</button>
            @if (loading) {
              <span class="text-sm text-neutral-500">Searching…</span>
            }
          </div>
        </form>

        @if (error) {
          <div class="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 mb-6">{{ error }}</div>
        }

        @if (results.length > 0) {
          <div class="text-sm text-neutral-600 mb-3">
            {{ totalElements }} place{{ totalElements === 1 ? '' : 's' }} found
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            @for (r of results; track r.id) {
              <a
                [routerLink]="['/locations', r.id]"
                class="bg-white rounded-2xl shadow-sm border border-neutral-100 p-5 hover:shadow-md transition"
              >
                <div class="flex items-start justify-between mb-2">
                  <h3 class="font-semibold text-neutral-900 truncate">{{ r.name }}</h3>
                  <span class="text-xs px-2 py-0.5 bg-neutral-100 rounded">{{ r.type }}</span>
                </div>
                @if (r.address) {
                  <p class="text-sm text-neutral-500 mb-2 truncate">{{ r.address }}</p>
                }
                @if (r.description) {
                  <p class="text-sm text-neutral-700 line-clamp-3 mb-3">{{ r.description }}</p>
                }
                <div class="flex items-center justify-between text-xs text-neutral-600">
                  <span>{{ r.reviewCount }} review{{ r.reviewCount === 1 ? '' : 's' }}</span>
                  @if (r.totalRating != null) {
                    <span>★ {{ r.totalRating.toFixed(1) }}</span>
                  }
                  @if (r.hasPdf) {
                    <span class="text-primary-600 font-semibold">PDF</span>
                  }
                </div>
              </a>
            }
          </div>
        } @else if (searched && !loading) {
          <div class="text-center text-neutral-500 py-12">No matching places.</div>
        }
      </div>
    </div>
  `,
})
export class PlaceSearchComponent {
  filters: {
    name?: string;
    description?: string;
    pdf?: string;
    reviewsFrom?: number | null;
    reviewsTo?: number | null;
  } = {};

  results: PlaceSearchResult[] = [];
  totalElements = 0;
  loading = false;
  searched = false;
  error = '';

  constructor(private search: PlaceSearchService) {}

  onSearch() {
    this.error = '';
    this.loading = true;
    this.searched = true;
    this.search
      .search({
        name: this.filters.name,
        description: this.filters.description,
        pdf: this.filters.pdf,
        reviewsFrom: this.filters.reviewsFrom ?? undefined,
        reviewsTo: this.filters.reviewsTo ?? undefined,
        size: 24,
      })
      .subscribe({
        next: (res) => {
          this.results = res.content || [];
          this.totalElements = res.totalElements;
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || 'Search failed';
          this.loading = false;
        },
      });
  }

  clear() {
    this.filters = {};
    this.results = [];
    this.totalElements = 0;
    this.searched = false;
    this.error = '';
  }
}
