import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { LocationService } from '../../services/location.service';

@Component({
  selector: 'app-location-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <section class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-3xl font-bold text-neutral-900">Edit location</h1>
            <p class="text-neutral-600 mt-1" *ngIf="name">{{ name }}</p>
            <p class="text-neutral-500 text-sm" *ngIf="!name">Loading...</p>
          </div>
          <a [routerLink]="['/locations', id]" class="btn-secondary">Back</a>
        </div>

        <div class="card p-6 space-y-6">
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-neutral-700 mb-2">Type</label>
                <input
                  class="input-field"
                  formControlName="type"
                  placeholder="Club, Bar, Hall..."
                />
              </div>
              <div>
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
            </div>

            <div class="flex items-center justify-end gap-3">
              <a [routerLink]="['/locations', id]" class="btn-secondary">Cancel</a>
              <button class="btn-primary" type="submit" [disabled]="form.invalid || saving">
                Save
              </button>
            </div>
          </form>
        </div>

        <div class="card p-6 mt-6">
          <h3 class="text-lg font-semibold mb-4">Change image</h3>
          <div class="flex items-center gap-4">
            <input type="file" (change)="onFileChange($event)" accept="image/*" />
            <button class="btn-secondary" (click)="uploadImage()" [disabled]="!image || uploading">
              Upload
            </button>
          </div>
          <p class="text-sm text-neutral-500 mt-2">Recommended: 1600x900, up to 15MB.</p>
        </div>
      </section>
    </div>
  `,
})
export class LocationEditComponent implements OnInit {
  id!: number;
  name: string | null = null;
  form: FormGroup;
  saving = false;
  uploading = false;
  image: File | null = null;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private locationService: LocationService,
    private toastr: ToastrService,
    private router: Router
  ) {
    this.form = this.fb.group({
      address: ['', [Validators.maxLength(200)]],
      type: ['', [Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]],
    });
  }

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    // Prepopulate with current data
    this.locationService.getLocation(this.id).subscribe({
      next: (loc) => {
        this.name = loc.name || null;
        this.form.patchValue({
          address: loc.address || '',
          type: loc.type || '',
          description: loc.description || '',
        });
      },
      error: () => {
        this.toastr.error('Failed to load location');
      },
    });
  }

  onSubmit(): void {
    const payload = { ...this.form.value } as any;
    Object.keys(payload).forEach((k) => payload[k] === '' && delete payload[k]);
    if (Object.keys(payload).length === 0) {
      this.toastr.info('Nothing to update');
      return;
    }
    this.saving = true;
    this.locationService.patchLocation(this.id, payload).subscribe({
      next: () => {
        this.toastr.success('Location updated');
        this.router.navigate(['/locations', this.id]);
      },
      error: (err) => this.toastr.error(err?.error?.message || 'Failed to update location'),
      complete: () => (this.saving = false),
    });
  }

  onFileChange(e: Event): void {
    const input = e.target as HTMLInputElement;
    if (input.files && input.files[0]) this.image = input.files[0];
  }

  uploadImage(): void {
    if (!this.image) return;
    this.uploading = true;
    this.locationService.updateLocationImage(this.id, this.image).subscribe({
      next: () => this.toastr.success('Image updated'),
      error: (err) => this.toastr.error(err?.error?.message || 'Failed to update image'),
      complete: () => (this.uploading = false),
    });
  }
}
