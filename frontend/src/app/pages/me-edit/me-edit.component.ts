import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../services/user.service';
import { UserProfile } from '../../models/user.model';

@Component({
  selector: 'app-me-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <section class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-3xl font-bold text-neutral-900">Edit profile</h1>
            <p class="text-neutral-600 mt-1">Update your information</p>
          </div>
          <a routerLink="/me" class="btn-secondary">Back to profile</a>
        </div>

        <div class="card p-6 space-y-6">
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-neutral-700 mb-2">Full name</label>
                <input formControlName="name" class="input-field" placeholder="Your name" />
              </div>
              <div>
                <label class="block text-sm font-medium text-neutral-700 mb-2">Phone</label>
                <input formControlName="phoneNumber" class="input-field" placeholder="+381..." />
              </div>
              <div>
                <label class="block text-sm font-medium text-neutral-700 mb-2">Birthday</label>
                <input type="date" formControlName="birthday" class="input-field" />
              </div>
              <div>
                <label class="block text-sm font-medium text-neutral-700 mb-2">City</label>
                <input formControlName="city" class="input-field" placeholder="Belgrade" />
              </div>
              <div class="md:col-span-2">
                <label class="block text-sm font-medium text-neutral-700 mb-2">Address</label>
                <input
                  formControlName="address"
                  class="input-field"
                  placeholder="Street and number"
                />
              </div>
            </div>

            <div class="flex items-center justify-between pt-2">
              <div class="text-sm text-neutral-500">
                Required fields: none — fill in what you prefer.
              </div>
              <div class="flex gap-3">
                <a routerLink="/me" class="btn-secondary">Cancel</a>
                <button class="btn-primary" type="submit" [disabled]="form.invalid || loading">
                  Save
                </button>
              </div>
            </div>
          </form>
        </div>

        <div class="card p-6 mt-6">
          <h3 class="text-lg font-semibold mb-4">Change avatar</h3>
          <div class="flex items-center gap-4">
            <input type="file" (change)="onFileChange($event)" accept="image/*" />
            <button
              class="btn-secondary"
              (click)="uploadAvatar()"
              [disabled]="!selectedFile || uploading"
            >
              Upload
            </button>
          </div>
          <p class="text-sm text-neutral-500 mt-2">Recommendation: square image, up to ~2MB.</p>
        </div>
      </section>
    </div>
  `,
  styles: [],
})
export class MeEditComponent {
  form: FormGroup;
  loading = false;
  uploading = false;
  selectedFile: File | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private toastr: ToastrService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.maxLength(100)]],
      phoneNumber: ['', [Validators.maxLength(20)]],
      birthday: [''],
      address: ['', [Validators.maxLength(200)]],
      city: ['', [Validators.maxLength(100)]],
    });

    this.loadProfile();
  }

  loadProfile(): void {
    this.userService.getMe().subscribe({
      next: (p) => {
        this.form.patchValue({
          name: p.name || '',
          phoneNumber: p.phoneNumber || '',
          birthday: p.birthday || '',
          address: p.address || '',
          city: p.city || '',
        });
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const payload = { ...this.form.value } as any;
    // empty strings -> undefined so PATCH won't overwrite
    Object.keys(payload).forEach((k) => payload[k] === '' && delete payload[k]);

    this.userService.updateMe(payload).subscribe({
      next: () => {
        this.toastr.success('Profile updated successfully');
        this.router.navigate(['/me']);
      },
      error: (err) => {
        const msg = err?.error?.message || 'Failed to update profile';
        this.toastr.error(msg);
      },
      complete: () => (this.loading = false),
    });
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
    }
  }

  uploadAvatar(): void {
    if (!this.selectedFile) return;
    this.uploading = true;
    this.userService.updateAvatar(this.selectedFile).subscribe({
      next: (res) => {
        this.toastr.success(res.message || 'Avatar updated');
        this.selectedFile = null;
        this.router.navigate(['/me']);
      },
      error: (err) => this.toastr.error(err?.error?.message || 'Failed to upload image'),
      complete: () => (this.uploading = false),
    });
  }
}
