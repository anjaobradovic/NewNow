import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService } from '../../services/user.service';
import { ReviewService } from '../../services/review.service';
import { ReviewDetailsDTO } from '../../models/user.model';

@Component({
  selector: 'app-manager-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-6xl mx-auto px-4">
        <h1 class="text-3xl font-bold mb-6">Moderate Reviews</h1>

        <div class="card p-4 mb-4">
          <label class="block text-sm text-neutral-700">Select your managed location</label>
          <select class="input-field w-full" [(ngModel)]="selectedLocationId" (change)="load()">
            <option [ngValue]="undefined" disabled>Select location</option>
            <option *ngFor="let l of managedLocations" [ngValue]="l.id">
              {{ l.locationName }}
            </option>
          </select>
          <p class="text-xs text-neutral-500 mt-2">Tip: izaberi lokaciju za moderaciju utisaka.</p>
        </div>

        <div *ngIf="reviews().length === 0" class="card p-8 text-center text-neutral-600">
          No reviews to moderate.
        </div>

        <div class="space-y-4">
          <div class="card p-4" *ngFor="let r of reviews()">
            <div class="flex items-start justify-between">
              <div>
                <div class="font-medium">Event: {{ r.event.name }}</div>
                <div class="text-sm text-neutral-500">{{ r.createdAt | date : 'medium' }}</div>
              </div>
              <div class="flex items-center gap-2">
                <button class="btn-secondary" (click)="toggleHidden(r)">
                  {{ r.hidden ? 'Unhide' : 'Hide' }}
                </button>
                <button class="btn-primary" (click)="deleteByManager(r)">Remove</button>
                <a class="text-primary-700 hover:underline" [routerLink]="['/reviews', r.id]"
                  >Open</a
                >
              </div>
            </div>
            <div class="mt-2 text-sm text-neutral-600">
              Avg: {{ r.ratings.average | number : '1.1-1' }}
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class ManagerReviewsComponent implements OnInit {
  managedLocations: Array<{ id: number; locationName: string }> = [];
  selectedLocationId?: number;
  reviews = signal<ReviewDetailsDTO[]>([]);

  constructor(private userService: UserService, private reviewService: ReviewService) {}

  ngOnInit(): void {
    this.userService.getManagedLocations().subscribe((list) => {
      this.managedLocations = list;
      this.selectedLocationId = list[0]?.id;
      this.load();
    });
  }

  load() {
    if (!this.selectedLocationId) return;
    this.reviewService
      .getLocationReviewsSorted(this.selectedLocationId, 'date', 'desc', 0, 20)
      .subscribe((res) => this.reviews.set(res.content || []));
  }

  toggleHidden(r: ReviewDetailsDTO) {
    this.reviewService.hideReview(r.id, !r.hidden).subscribe(() => this.load());
  }

  deleteByManager(r: ReviewDetailsDTO) {
    this.reviewService.deleteByManager(r.id).subscribe(() => this.load());
  }
}
