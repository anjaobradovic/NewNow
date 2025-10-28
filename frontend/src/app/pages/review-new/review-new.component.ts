import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReviewService } from '../../services/review.service';
import { EventService } from '../../services/event.service';
import { Event } from '../../models/event.model';
import { CreateReviewRequest } from '../../models/user.model';

@Component({
  selector: 'app-review-new',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-3xl mx-auto px-4">
        <a [routerLink]="['/locations', locationId]" class="text-primary-700 hover:underline"
          >&larr; Back to location</a
        >
        <h1 class="text-3xl font-bold text-neutral-900 mt-4 mb-6">Write a review</h1>

        <div class="card p-6 space-y-6">
          <div>
            <label class="block text-sm font-medium text-neutral-700 mb-1">Select event</label>
            <select class="input-field w-full" [(ngModel)]="form.eventId">
              <option [ngValue]="undefined" disabled>Select an event</option>
              <option *ngFor="let e of events" [ngValue]="e.id">{{ e.name }} • {{ e.date }}</option>
            </select>
            <p class="text-xs text-neutral-500 mt-1">
              Only regular (recurrent) events that are active (today) or past can be reviewed.
            </p>
          </div>

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
            <label class="block text-sm font-medium text-neutral-700">Comment (optional)</label>
            <textarea
              class="input-field w-full h-28"
              placeholder="Share your experience..."
              [(ngModel)]="form.comment"
            ></textarea>
          </div>

          <div class="flex items-center justify-between">
            <button class="btn-secondary" [routerLink]="['/locations', locationId]">Cancel</button>
            <button class="btn-primary" (click)="submit()">Publish review</button>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class ReviewNewComponent {
  locationId!: number;
  events: Event[] = [];

  form: CreateReviewRequest = {
    eventId: undefined as any,
    performance: null,
    soundAndLighting: null,
    venue: null,
    overallImpression: null,
    comment: '',
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reviewService: ReviewService,
    private eventService: EventService
  ) {}

  ngOnInit() {
    this.locationId = Number(this.route.snapshot.paramMap.get('locationId'));
    this.eventService
      .searchEvents({ locationId: this.locationId, page: 0, size: 20 })
      .subscribe((res) => (this.events = res.content || []));
  }

  submit() {
    if (!this.form.eventId) {
      alert('Please select an event');
      return;
    }
    this.reviewService.createReview(this.locationId, this.form).subscribe({
      next: (r) => this.router.navigate(['/reviews', r.id]),
      error: (err) => {
        const message = err.error?.message || 'Failed to submit review. Please try again.';
        alert(message);
      },
    });
  }
}
