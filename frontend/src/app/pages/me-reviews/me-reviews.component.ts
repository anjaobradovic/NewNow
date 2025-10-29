import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService } from '../../services/user.service';
import { PageResponse, ReviewDTO } from '../../models/user.model';

@Component({
  selector: 'app-me-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  providers: [DatePipe],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <section class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-3xl font-bold text-neutral-900">My reviews</h1>
            <p class="text-neutral-600 mt-1">List of your reviews</p>
          </div>
        </div>

        <div class="card p-4 mb-6">
          <div class="flex flex-wrap gap-3 items-center">
            <label class="text-sm text-neutral-600">Sort by:</label>
            <select class="input-field w-auto" [(ngModel)]="sort" (ngModelChange)="reload()">
              <option value="date">Date</option>
              <option value="rating">Rating</option>
            </select>
            <select class="input-field w-auto" [(ngModel)]="order" (ngModelChange)="reload()">
              <option value="desc">Descending</option>
              <option value="asc">Ascending</option>
            </select>
          </div>
          <p class="text-xs text-neutral-500 mt-2">
            Savjet: koristi sortiranje da lakše upravljaš svojim utiscima.
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <a
            [routerLink]="['/reviews', r.id]"
            class="card p-6 hover:shadow-xl transition-shadow cursor-pointer"
            *ngFor="let r of reviews()"
          >
            <div class="flex items-start justify-between">
              <div>
                <h3 class="text-lg font-semibold">{{ r.eventName }}</h3>
                <p class="text-neutral-600 text-sm">{{ r.locationName }}</p>
              </div>
              <div class="text-right">
                <div class="text-xl font-bold text-primary-700">
                  {{ r.rate.averageRating.toFixed(1) }}
                </div>
                <div class="text-xs text-neutral-500">{{ r.createdAt | date : 'mediumDate' }}</div>
              </div>
            </div>
            <div class="mt-4 grid grid-cols-2 gap-3 text-sm">
              <div class="flex justify-between">
                <span>Performance</span><span class="font-semibold">{{ r.rate.performance }}</span>
              </div>
              <div class="flex justify-between">
                <span>Sound & Light</span
                ><span class="font-semibold">{{ r.rate.soundAndLighting }}</span>
              </div>
              <div class="flex justify-between">
                <span>Space</span><span class="font-semibold">{{ r.rate.venue }}</span>
              </div>
              <div class="flex justify-between">
                <span>Overall</span
                ><span class="font-semibold">{{ r.rate.overallImpression }}</span>
              </div>
            </div>
            <div class="mt-3 text-xs text-blue-600">
              <i class="fas fa-repeat mr-1"></i>Posećeno {{ r.eventCount }}x do tada
            </div>
            <div class="mt-3 flex items-center gap-2">
              <span
                class="px-2 py-1 rounded-full text-xs"
                [class.bg-neutral-200]="!r.hidden"
                [class.bg-yellow-100]="r.hidden"
                [class.text-neutral-700]="!r.hidden"
                [class.text-yellow-800]="r.hidden"
                >{{ r.hidden ? 'Hidden' : 'Visible' }}</span
              >
              <span class="text-xs text-primary-600">
                <i class="fas fa-arrow-right ml-1"></i>Otvori diskusiju
              </span>
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
  styles: [],
})
export class MeReviewsComponent implements OnInit {
  reviews = signal<ReviewDTO[]>([]);
  page = 0;
  size = 10;
  totalPages = 1;
  sort: 'rating' | 'date' = 'date';
  order: 'asc' | 'desc' = 'desc';

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.userService.getMyReviews(this.page, this.size, this.sort, this.order).subscribe({
      next: (res) => {
        this.reviews.set(res.content);
        this.totalPages = res.totalPages || 1;
      },
    });
  }

  prev(): void {
    if (this.page > 0) {
      this.page--;
      this.reload();
    }
  }

  next(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.reload();
    }
  }
}
