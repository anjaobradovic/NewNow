import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReviewService } from '../../services/review.service';
import { ReviewDetailsDTO, UpdateReviewRequest } from '../../models/user.model';

@Component({
  selector: 'app-review-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-3xl mx-auto px-4">
        <a [routerLink]="['/reviews', id]" class="text-primary-700 hover:underline"
          >&larr; Back to review</a
        >
        <h1 class="text-3xl font-bold text-neutral-900 mt-4 mb-6">Edit your review</h1>

        <div *ngIf="!loaded" class="card p-6 animate-pulse">
          <div class="h-6 bg-neutral-200 w-1/3 rounded"></div>
          <div class="h-4 bg-neutral-200 w-1/2 rounded mt-3"></div>
        </div>

        <div *ngIf="loaded" class="card p-6 space-y-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-neutral-700"
                >Performance (optional)</label
              >
              <input
                type="number"
                class="input-field w-full"
                min="1"
                max="10"
                placeholder="Rate 1-10 or leave empty"
                [(ngModel)]="form.performance"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-neutral-700"
                >Sound & Lighting (optional)</label
              >
              <input
                type="number"
                class="input-field w-full"
                min="1"
                max="10"
                placeholder="Rate 1-10 or leave empty"
                [(ngModel)]="form.soundAndLighting"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-neutral-700">Venue (optional)</label>
              <input
                type="number"
                class="input-field w-full"
                min="1"
                max="10"
                placeholder="Rate 1-10 or leave empty"
                [(ngModel)]="form.venue"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-neutral-700"
                >Overall Impression (optional)</label
              >
              <input
                type="number"
                class="input-field w-full"
                min="1"
                max="10"
                placeholder="Rate 1-10 or leave empty"
                [(ngModel)]="form.overallImpression"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-neutral-700">Comment</label>
            <textarea class="input-field w-full h-28" [(ngModel)]="form.comment"></textarea>
          </div>

          <div class="flex items-center justify-between">
            <a class="btn-secondary" [routerLink]="['/reviews', id]">Cancel</a>
            <button class="btn-primary" (click)="save()">Save changes</button>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class ReviewEditComponent implements OnInit {
  id!: number;
  loaded = false;
  form: UpdateReviewRequest = {
    performance: null,
    soundAndLighting: null,
    venue: null,
    overallImpression: null,
    comment: '',
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.reviewService.getReview(this.id).subscribe({
      next: (r: ReviewDetailsDTO) => {
        this.form = {
          performance: r.ratings.performance,
          soundAndLighting: r.ratings.soundAndLighting,
          venue: r.ratings.venue,
          overallImpression: r.ratings.overallImpression,
          comment: r.comment || '',
        };
        this.loaded = true;
      },
      error: (err) => {
        const message = err.error?.message || 'Failed to load review';
        alert(message);
        this.router.navigate(['/']);
      },
    });
  }

  save() {
    this.reviewService.updateReview(this.id, this.form).subscribe({
      next: (r) => {
        this.router.navigate(['/reviews', r.id]);
      },
      error: (err) => {
        const message = err.error?.message || 'Failed to update review. Please try again.';
        alert(message);
      },
    });
  }
}
