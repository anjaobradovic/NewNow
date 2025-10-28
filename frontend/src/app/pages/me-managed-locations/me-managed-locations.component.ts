import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../../services/user.service';
import { ManagedLocationDTO } from '../../models/user.model';

@Component({
  selector: 'app-me-managed-locations',
  standalone: true,
  imports: [CommonModule, RouterLink],
  providers: [DatePipe],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <section class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-neutral-900">Managed locations</h1>
          <p class="text-neutral-600 mt-1">Overview of locations you manage</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="card p-6" *ngFor="let m of locations()">
            <div class="flex items-start justify-between mb-4">
              <div>
                <h3 class="text-xl font-semibold">{{ m.locationName }}</h3>
                <p class="text-neutral-600">{{ m.locationAddress }}</p>
                <div class="mt-2">
                  <span class="px-3 py-1 rounded-full text-xs bg-primary-100 text-primary-700">{{
                    m.locationType
                  }}</span>
                </div>
              </div>
              <div class="text-right">
                <div
                  class="text-sm"
                  [class.text-green-700]="m.isActive"
                  [class.text-neutral-600]="!m.isActive"
                >
                  {{ m.isActive ? 'Active manager' : 'Not active' }}
                </div>
                <div class="text-xs text-neutral-500 mt-1">From: {{ m.startDate || '—' }}</div>
                <div class="text-xs text-neutral-500">To: {{ m.endDate || '—' }}</div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex gap-2 pt-4 border-t border-neutral-100">
              <a
                [routerLink]="['/locations', m.id]"
                class="flex-1 text-center px-4 py-2 text-sm font-medium text-primary-600 hover:text-primary-700 hover:bg-primary-50 rounded-xl transition-all"
              >
                View Details
              </a>
              <a
                [routerLink]="['/analytics/locations', m.id]"
                class="flex-1 text-center px-4 py-2 text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 rounded-xl transition-all"
              >
                Analytics
              </a>
            </div>
            <div class="flex gap-2 pt-2">
              <a
                routerLink="/manager/reviews"
                class="flex-1 text-center px-4 py-2 text-sm font-medium text-purple-600 hover:text-purple-700 hover:bg-purple-50 rounded-xl transition-all flex items-center justify-center gap-2"
              >
                <svg
                  class="w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                  />
                </svg>
                Moderate Reviews
              </a>
            </div>
          </div>
        </div>

        <div *ngIf="locations().length === 0" class="card p-12 text-center text-neutral-600 mt-6">
          You don't manage any locations.
        </div>
      </section>
    </div>
  `,
  styles: [],
})
export class MeManagedLocationsComponent implements OnInit {
  locations = signal<ManagedLocationDTO[]>([]);

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.userService.getManagedLocations().subscribe({
      next: (res) => this.locations.set(res || []),
    });
  }
}
