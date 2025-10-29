import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventService } from '../../services/event.service';
import { UserService } from '../../services/user.service';
import { ManagedLocationDTO } from '../../models/user.model';

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
                @if (currentImageUrl) {
                <div class="mb-2">
                  <img
                    [src]="imageSrc(currentImageUrl)"
                    alt="Current event image"
                    class="h-32 w-auto object-cover rounded border border-neutral-200"
                  />
                  <p class="text-xs text-neutral-500 mt-1">Current image</p>
                </div>
                }
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
  locationId = 0;
  name = '';
  address = '';
  type = '';
  date = '';
  price: number | undefined;
  recurrent = false;
  image?: File;
  submitting = false;
  currentImageUrl = ''; // Track current image URL

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: EventService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getEvent(this.eventId).subscribe({
      next: (e) => {
        this.locationId = e.locationId;
        this.name = e.name;
        this.address = e.address;
        this.type = e.type;
        this.date = e.date;
        this.price = e.price;
        this.recurrent = e.recurrent;
        this.currentImageUrl = e.imageUrl || ''; // Store current image URL
        this.checkPermissions();
      },
      error: () => {
        alert('Event not found');
        this.router.navigate(['/events']);
      },
    });
  }

  checkPermissions(): void {
    try {
      const user = JSON.parse(localStorage.getItem('user_data') || 'null');
      if (!user?.roles?.includes('ROLE_MANAGER') && !user?.roles?.includes('ROLE_ADMIN')) {
        alert('Only managers can edit events');
        this.router.navigate(['/events', this.eventId]);
        return;
      }

      this.userService.getManagedLocations().subscribe({
        next: (locations: ManagedLocationDTO[]) => {
          const isManager = locations.some((loc: ManagedLocationDTO) => loc.id === this.locationId);
          if (!isManager) {
            alert('You can only edit events from locations you manage');
            this.router.navigate(['/events', this.eventId]);
          }
        },
        error: () => {
          alert('Failed to verify permissions');
          this.router.navigate(['/events', this.eventId]);
        },
      });
    } catch {
      alert('Authentication error');
      this.router.navigate(['/events', this.eventId]);
    }
  }

  onFile(e: Event): void {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.image = file;
  }

  updateImage(): void {
    if (!this.image) return;
    this.service.updateEventImage(this.eventId, this.image).subscribe({
      next: (updatedEvent) => {
        this.currentImageUrl = updatedEvent.imageUrl || '';
        alert('Image updated successfully');
        // Clear the file input
        this.image = undefined;
      },
      error: () => alert('Failed to update image'),
    });
  }

  imageSrc(url?: string): string {
    if (!url) return '/assets/placeholder.jpg';
    if (url.startsWith('http')) return url;
    return `http://localhost:8080${url}`;
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
