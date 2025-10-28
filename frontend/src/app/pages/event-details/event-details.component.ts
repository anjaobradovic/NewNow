import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../services/event.service';
import { UserService } from '../../services/user.service';
import { Event } from '../../models/event.model';
import { ManagedLocationDTO } from '../../models/user.model';

@Component({
  selector: 'app-event-details',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-5xl mx-auto px-4">
        <a routerLink="/events" class="text-primary-600 hover:text-primary-700">← Back to events</a>

        <div *ngIf="loading()" class="card p-10 mt-4">
          <div class="h-64 bg-neutral-200 animate-pulse"></div>
        </div>

        <div *ngIf="!loading() && event() as e" class="card overflow-hidden mt-4">
          <div class="h-72 bg-neutral-200">
            <img *ngIf="e.imageUrl" [src]="e.imageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="p-6">
            <div class="flex items-start justify-between gap-4">
              <div>
                <h1 class="text-3xl font-bold text-neutral-900">{{ e.name }}</h1>
                <div class="text-neutral-600 mt-1">{{ e.type }} • {{ e.date }}</div>
                <div class="text-neutral-600">{{ e.locationName }} • {{ e.address }}</div>
              </div>
              <div class="text-right flex flex-col gap-2">
                <div class="text-2xl font-semibold text-primary-700">
                  {{ e.price ? e.price + ' RSD' : 'Free' }}
                </div>
                <div class="text-xs text-neutral-500">
                  {{ e.recurrent ? 'Regular event' : 'One-time' }}
                </div>
                <a
                  *ngIf="isManager"
                  [routerLink]="['/events', e.id, 'edit']"
                  class="btn-primary text-sm"
                >
                  Edit Event
                </a>
              </div>
            </div>

            <div class="mt-6 p-4 bg-neutral-50 rounded-xl border border-neutral-100">
              <label class="text-sm text-neutral-600">Occurrences until</label>
              <div class="flex items-center gap-3 mt-2">
                <input class="input-field" type="date" [(ngModel)]="untilDate" />
                <button class="btn-primary" (click)="loadCount()">Check</button>
                <span *ngIf="occCount !== null" class="text-neutral-700">
                  Total times held: <b>{{ occCount }}</b>
                </span>
              </div>
              <div *ngIf="errorMsg" class="text-sm text-red-600 mt-2">{{ errorMsg }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class EventDetailsComponent implements OnInit {
  event = signal<Event | null>(null);
  loading = signal(true);
  untilDate = '';
  occCount: number | null = null;
  errorMsg = '';
  isManager = false;

  constructor(
    private route: ActivatedRoute,
    private service: EventService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getEvent(id).subscribe({
      next: (e) => {
        this.event.set(e);
        this.loading.set(false);
        this.checkPermissions(e.locationId);
      },
      error: () => this.loading.set(false),
    });
  }

  checkPermissions(locationId: number): void {
    try {
      const user = JSON.parse(localStorage.getItem('user_data') || 'null');
      if (user?.roles?.includes('ROLE_MANAGER') || user?.roles?.includes('ROLE_ADMIN')) {
        this.userService.getManagedLocations().subscribe({
          next: (locations: ManagedLocationDTO[]) => {
            this.isManager = locations.some((loc: ManagedLocationDTO) => loc.id === locationId);
          },
          error: () => {
            this.isManager = false;
          },
        });
      }
    } catch {}
  }

  loadCount(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.untilDate) {
      this.errorMsg = 'Select a date';
      return;
    }
    this.errorMsg = '';
    this.service.countOccurrences(id, this.untilDate).subscribe({
      next: (res) => (this.occCount = res.count),
      error: () => (this.errorMsg = 'Failed to load count'),
    });
  }
}
