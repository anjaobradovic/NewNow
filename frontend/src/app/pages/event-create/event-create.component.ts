import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventService } from '../../services/event.service';
import { UserService } from '../../services/user.service';
import { ManagedLocationDTO } from '../../models/user.model';

@Component({
  selector: 'app-event-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-10">
      <div class="max-w-3xl mx-auto px-4">
        <a [routerLink]="['/locations', locationId]" class="text-primary-600 hover:text-primary-700"
          >← Back to location</a
        >
        <h1 class="text-3xl font-bold text-neutral-900 mt-4">Create Event</h1>
        <p class="text-neutral-600 mb-6">Publish a new experience at your venue</p>

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
            <div>
              <label class="block text-sm text-neutral-600 mb-1">Cover image</label>
              <input type="file" accept="image/*" (change)="onFile($event)" />
              <div *ngIf="preview" class="mt-3">
                <img [src]="preview" class="w-full h-56 object-cover rounded-xl border" />
              </div>
            </div>
          </div>

          <div class="flex items-center gap-3 pt-2">
            <button type="submit" class="btn-primary" [disabled]="submitting">Create</button>
            <a [routerLink]="['/locations', locationId]" class="btn-secondary">Cancel</a>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class EventCreateComponent implements OnInit {
  locationId = 0;

  name = '';
  address = '';
  type = '';
  date = '';
  price: number | undefined;
  recurrent = false;
  image?: File;
  preview?: string;
  submitting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: EventService,
    private userService: UserService
  ) {
    this.locationId = Number(this.route.snapshot.paramMap.get('locationId'));
  }

  ngOnInit(): void {
    this.checkPermissions();
  }

  checkPermissions(): void {
    try {
      const user = JSON.parse(localStorage.getItem('user_data') || 'null');
      if (!user?.roles?.includes('ROLE_MANAGER')) {
        alert('Only managers can create events');
        this.router.navigate(['/locations', this.locationId]);
        return;
      }

      this.userService.getManagedLocations().subscribe({
        next: (locations: ManagedLocationDTO[]) => {
          const isManager = locations.some((loc: ManagedLocationDTO) => loc.id === this.locationId);
          if (!isManager) {
            alert('You can only create events for locations you manage');
            this.router.navigate(['/locations', this.locationId]);
          }
        },
        error: () => {
          alert('Failed to verify permissions');
          this.router.navigate(['/locations', this.locationId]);
        },
      });
    } catch {
      alert('Authentication error');
      this.router.navigate(['/locations', this.locationId]);
    }
  }

  onFile(e: Event): void {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      this.image = file;
      const reader = new FileReader();
      reader.onload = () => (this.preview = reader.result as string);
      reader.readAsDataURL(file);
    }
  }

  submit(): void {
    if (!this.name || !this.address || !this.type || !this.date || !this.image) {
      alert('Popunite sva polja i izaberite sliku');
      return;
    }
    this.submitting = true;
    this.service
      .createEvent(this.locationId, {
        name: this.name.trim(),
        address: this.address.trim(),
        type: this.type.trim(),
        date: this.date,
        price: this.price ?? 0,
        recurrent: this.recurrent,
        image: this.image!,
      })
      .subscribe({
        next: (e) => this.router.navigate(['/events', e.id]),
        error: () => {
          this.submitting = false;
          alert('Neuspelo kreiranje događaja');
        },
      });
  }
}
