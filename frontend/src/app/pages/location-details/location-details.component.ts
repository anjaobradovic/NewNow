import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LocationService } from '../../services/location.service';
import { UserService } from '../../services/user.service';
import { LocationDetailsDTO } from '../../models/location.model';
import { FormsModule } from '@angular/forms';
import { Event } from '../../models/event.model';
import { ReviewDTO } from '../../models/user.model';

@Component({
  selector: 'app-location-details',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  providers: [DatePipe],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10" *ngIf="location() as loc">
        <div class="flex flex-wrap items-center justify-between gap-4 mb-6">
          <div>
            <h1 class="text-3xl font-bold text-neutral-900">{{ loc.name }}</h1>
            <p class="text-neutral-600">{{ loc.address }} • {{ loc.type }}</p>
          </div>
          <div class="flex gap-3">
            <a
              *ngIf="isAdmin || isManager"
              [routerLink]="['/locations', loc.id, 'edit']"
              class="btn-secondary"
              >Edit</a
            >
            <a
              *ngIf="isAdmin || isManager"
              [routerLink]="['/analytics/locations', loc.id]"
              class="btn-secondary"
              >Analytics</a
            >
            <a *ngIf="isAdmin" [routerLink]="['/locations', loc.id, 'managers']" class="btn-primary"
              >Managers</a
            >
            <button
              *ngIf="isAdmin"
              (click)="deleteLocation()"
              class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-xl font-medium transition-colors"
              [disabled]="deleting()"
            >
              {{ deleting() ? 'Deleting...' : 'Delete' }}
            </button>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div class="lg:col-span-2 card overflow-hidden">
            <div class="h-72 w-full bg-neutral-100">
              <img
                *ngIf="loc.imageUrl"
                [src]="imageSrc(loc.imageUrl)"
                class="w-full h-full object-cover"
              />
            </div>
            <div class="p-6">
              <div class="flex flex-wrap items-center gap-4 text-neutral-600 text-sm">
                <span>Created: {{ loc.createdAt || '—' }}</span>
                <span>Avg rating: {{ (loc.averageRating || 0).toFixed(1) }}</span>
                <span>Total reviews: {{ loc.totalReviews || 0 }}</span>
              </div>
              <p
                class="mt-4 text-neutral-700"
                [innerText]="loc.description || 'No description'"
              ></p>
            </div>
          </div>

          <div class="card p-6">
            <h3 class="text-lg font-semibold mb-4">Upcoming events</h3>
            <div class="space-y-3 max-h-80 overflow-auto pr-2">
              <div *ngFor="let e of events()" class="p-3 border border-neutral-100 rounded-2xl">
                <div class="flex items-center justify-between gap-2">
                  <div class="flex-1">
                    <div class="font-medium">{{ e.name }}</div>
                    <div class="text-xs text-neutral-500">{{ e.date }}</div>
                  </div>
                  <div class="flex items-center gap-2">
                    <div class="text-sm text-primary-700">
                      {{ e.price ? e.price + ' RSD' : 'Free' }}
                    </div>
                    <a
                      *ngIf="isManager"
                      [routerLink]="['/events', e.id, 'edit']"
                      class="px-3 py-1.5 bg-primary-100 hover:bg-primary-200 text-primary-700 rounded-lg text-xs font-medium transition-colors"
                      title="Edit event"
                    >
                      Edit
                    </a>
                  </div>
                </div>
              </div>
              <div *ngIf="events().length === 0" class="text-sm text-neutral-500">
                No upcoming events
              </div>
            </div>
            <div class="mt-4" *ngIf="isManager">
              <a [routerLink]="['/locations', location()?.id, 'events', 'new']" class="btn-primary"
                >Add event</a
              >
            </div>
          </div>
        </div>

        <div class="card p-6 mt-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold">Reviews</h3>
            <div class="flex gap-2">
              <a class="btn-primary" [routerLink]="['/locations', loc.id, 'reviews', 'new']"
                >Write a review</a
              >
              <select class="input-field w-auto" [(ngModel)]="sort" (ngModelChange)="loadReviews()">
                <option value="date">Date</option>
                <option value="rating">Rating</option>
              </select>
              <select
                class="input-field w-auto"
                [(ngModel)]="order"
                (ngModelChange)="loadReviews()"
              >
                <option value="desc">Desc</option>
                <option value="asc">Asc</option>
              </select>
            </div>
          </div>
          <p class="text-xs text-neutral-500 mb-4">
            Savjet: sortiraj po oceni ili datumu da brže pronađeš relevantne utiske.
          </p>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="p-4 border border-neutral-100 rounded-2xl" *ngFor="let r of reviews()">
              <div class="flex items-start justify-between">
                <div>
                  <div class="font-medium">{{ r.eventName }}</div>
                  <div class="text-xs text-neutral-500">
                    {{ r.createdAt | date : 'mediumDate' }}
                  </div>
                </div>
                <div class="text-primary-700 font-bold">
                  {{ r.rate.averageRating.toFixed(1) }}
                </div>
              </div>
              <div class="mt-3 grid grid-cols-2 gap-2 text-sm">
                <div class="flex justify-between">
                  <span>Perf.</span><span>{{ r.rate.performance }}</span>
                </div>
                <div class="flex justify-between">
                  <span>Sound</span><span>{{ r.rate.soundAndLighting }}</span>
                </div>
                <div class="flex justify-between">
                  <span>Venue</span><span>{{ r.rate.venue }}</span>
                </div>
                <div class="flex justify-between">
                  <span>Overall</span><span>{{ r.rate.overallImpression }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="flex items-center justify-center gap-3 mt-6">
            <button class="btn-secondary" (click)="prevR()" [disabled]="pageR === 0">Back</button>
            <div class="text-sm text-neutral-600">Page {{ pageR + 1 }} of {{ totalPagesR }}</div>
            <button class="btn-primary" (click)="nextR()" [disabled]="pageR + 1 >= totalPagesR">
              Next
            </button>
          </div>
        </div>
      </section>
    </div>
  `,
})
export class LocationDetailsComponent implements OnInit {
  location = signal<LocationDetailsDTO | null>(null);
  events = signal<Event[]>([]);
  reviews = signal<ReviewDTO[]>([]);
  deleting = signal<boolean>(false);

  pageR = 0;
  sizeR = 6;
  totalPagesR = 1;
  sort: 'rating' | 'date' = 'date';
  order: 'asc' | 'desc' = 'desc';

  isAdmin = false;
  isManager = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private locationService: LocationService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
    try {
      const user = JSON.parse(localStorage.getItem('user_data') || 'null');
      this.isAdmin = !!user?.roles?.includes('ROLE_ADMIN');

      // Check if user is manager of this location
      if (user && user.roles?.includes('ROLE_MANAGER')) {
        this.userService.getManagedLocations().subscribe({
          next: (locations) => {
            this.isManager = locations.some((loc) => loc.id === id);
          },
          error: () => {
            this.isManager = false;
          },
        });
      }
    } catch {}
  }

  load(id: number): void {
    this.locationService.getLocation(id).subscribe({
      next: (loc) => this.location.set(loc),
    });

    this.locationService.getUpcomingEvents(id, 0, 6).subscribe({
      next: (res: any) => {
        // Backend returns Page<EventDTO> for upcoming events; map to content if present
        const content = Array.isArray(res?.content) ? res.content : res || [];
        this.events.set(content);
      },
    });

    this.loadReviews();
  }

  loadReviews(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.locationService
      .getLocationReviews(id, this.sort, this.order, this.pageR, this.sizeR)
      .subscribe({
        next: (res) => {
          this.reviews.set(res.content || []);
          this.totalPagesR = res.totalPages || 1;
        },
      });
  }

  prevR(): void {
    if (this.pageR > 0) {
      this.pageR--;
      this.loadReviews();
    }
  }
  nextR(): void {
    if (this.pageR + 1 < this.totalPagesR) {
      this.pageR++;
      this.loadReviews();
    }
  }

  deleteLocation(): void {
    if (!this.location()) return;

    const confirmed = confirm(
      `Are you sure you want to permanently delete "${
        this.location()?.name
      }"? This will delete all events, reviews, and data associated with this location. This action cannot be undone.`
    );

    if (!confirmed) return;

    this.deleting.set(true);
    const id = this.location()!.id;

    this.locationService.deleteLocation(id).subscribe({
      next: () => {
        alert('Location deleted successfully');
        this.router.navigate(['/locations']);
      },
      error: (err) => {
        console.error('Delete failed:', err);
        this.deleting.set(false);
        const message =
          err.error?.message || err.message || 'Failed to delete location. Please try again.';
        alert(message);
      },
    });
  }

  imageSrc(url?: string): string | undefined {
    if (!url) return undefined;
    const isDev = typeof window !== 'undefined' && window.location.port === '4200';
    if (isDev && url.startsWith('/uploads/')) return `http://localhost:8080${url}`;
    return url;
  }
}
