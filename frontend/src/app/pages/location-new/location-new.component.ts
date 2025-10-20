import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { LocationService } from '../../services/location.service';

@Component({
  selector: 'app-location-new',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <section class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-3xl font-bold text-neutral-900">Create location</h1>
            <p class="text-neutral-600 mt-1">Enter details and upload a cover image</p>
          </div>
          <a routerLink="/locations" class="btn-secondary">Back</a>
        </div>

        <div class="card p-6">
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-neutral-700 mb-2">Name</label>
                <input class="input-field" formControlName="name" placeholder="Venue name" />
              </div>
              <div>
                <label class="block text-sm font-medium text-neutral-700 mb-2">Type</label>
                <input
                  class="input-field"
                  formControlName="type"
                  placeholder="Club, Bar, Hall..."
                />
              </div>
              <div class="md:col-span-2">
                <label class="block text-sm font-medium text-neutral-700 mb-2">Address</label>
                <input class="input-field" formControlName="address" placeholder="Street, number" />
              </div>
              <div class="md:col-span-2">
                <label class="block text-sm font-medium text-neutral-700 mb-2">Description</label>
                <textarea
                  class="input-field"
                  formControlName="description"
                  rows="4"
                  placeholder="Short description"
                ></textarea>
              </div>
              <div class="md:col-span-2">
                <label class="block text-sm font-medium text-neutral-700 mb-2">Image</label>
                <input type="file" (change)="onFileChange($event)" accept="image/*" />
              </div>
            </div>

            <div class="flex items-center justify-end gap-3">
              <a routerLink="/locations" class="btn-secondary">Cancel</a>
              <button class="btn-primary" type="submit" [disabled]="form.invalid || !image">
                Create
              </button>
            </div>
          </form>
        </div>
      </section>
    </div>
  `,
})
export class LocationNewComponent {
  form: FormGroup;
  image: File | null = null;

  constructor(
    private fb: FormBuilder,
    private locationService: LocationService,
    private toastr: ToastrService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      address: ['', [Validators.required, Validators.maxLength(200)]],
      type: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]],
    });
  }

  onFileChange(e: Event): void {
    const input = e.target as HTMLInputElement;
    if (input.files && input.files[0]) this.image = input.files[0];
  }

  onSubmit(): void {
    if (this.form.invalid || !this.image) return;
    const { name, address, type, description } = this.form.value;
    this.locationService
      .createLocation({ name, address, type, description, image: this.image })
      .subscribe({
        next: (loc) => {
          this.toastr.success('Location created');
          this.router.navigate(['/locations', loc.id]);
        },
        error: (err) => this.toastr.error(err?.error?.message || 'Failed to create location'),
      });
  }
}
