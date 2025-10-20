import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventService } from '../../services/event.service';

@Component({
  selector: 'app-event-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-3xl mx-auto px-4">
        <a [routerLink]="['/events', eventId]" class="text-primary-600 hover:text-primary-700"
          >← Back to details</a
        >
        <h1 class="text-3xl font-bold text-neutral-900 mt-4">Edit Event</h1>
        <p class="text-neutral-600 mb-6">Update information and imagery</p>

        <form class="card p-6 space-y-4" (ngSubmit)="submit()">
          <div class="grid grid-cols-1 gap-4">
            <div>
              <label class="block text-sm text-neutral-600 mb-1">Name</label>
              <input type="text" class="input-field" [(ngModel)]="name" name="name" required />
            </div>
            <div>
              <label class="block text-sm text-neutral-600 mb-1">Address</label>
              <input
                type="text"
                class="input-field"
                [(ngModel)]="address"
                name="address"
                required
              />
            </div>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="block text-sm text-neutral-600 mb-1">Type</label>
                <input type="text" class="input-field" [(ngModel)]="type" name="type" required />
              </div>
              <div>
                <label class="block text-sm text-neutral-600 mb-1">Date</label>
                <input type="date" class="input-field" [(ngModel)]="date" name="date" required />
              </div>
              <div>
                <label class="block text-sm text-neutral-600 mb-1">Price (RSD)</label>
                <input type="number" min="0" class="input-field" [(ngModel)]="price" name="price" />
              </div>
            </div>
            <label class="inline-flex items-center gap-2 text-sm text-neutral-700">
              <input type="checkbox" [(ngModel)]="recurrent" name="recurrent" /> Regular event
            </label>
            <div class="grid grid-cols-1 md:grid-cols-[1fr_auto] gap-3 items-end">
              <div>
                <label class="block text-sm text-neutral-600 mb-1">Replace cover image</label>
                <input type="file" accept="image/*" (change)="onFile($event)" />
              </div>
              <button
                type="button"
                class="btn-secondary"
                (click)="updateImage()"
                [disabled]="!image"
              >
                Upload image
              </button>
            </div>
          </div>

          <div class="flex items-center gap-3 pt-2">
            <button type="submit" class="btn-primary" [disabled]="submitting">Save</button>
            <a [routerLink]="['/events', eventId]" class="btn-secondary">Cancel</a>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class EventEditComponent implements OnInit {
  eventId = 0;
  name = '';
  address = '';
  type = '';
  date = '';
  price: number | undefined;
  recurrent = false;
  image?: File;
  submitting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: EventService
  ) {}

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getEvent(this.eventId).subscribe((e) => {
      this.name = e.name;
      this.address = e.address;
      this.type = e.type;
      this.date = e.date;
      this.price = e.price;
      this.recurrent = e.recurrent;
    });
  }

  onFile(e: Event): void {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.image = file;
  }

  updateImage(): void {
    if (!this.image) return;
    this.service.updateEventImage(this.eventId, this.image).subscribe({
      next: () => alert('Image updated'),
      error: () => alert('Failed to update image'),
    });
  }

  submit(): void {
    this.submitting = true;
    this.service
      .updateEvent(this.eventId, {
        name: this.name.trim(),
        address: this.address.trim(),
        type: this.type.trim(),
        date: this.date,
        price: this.price ?? 0,
        recurrent: this.recurrent,
      })
      .subscribe({
        next: () => this.router.navigate(['/events', this.eventId]),
        error: () => {
          this.submitting = false;
          alert('Failed to save changes');
        },
      });
  }
}
