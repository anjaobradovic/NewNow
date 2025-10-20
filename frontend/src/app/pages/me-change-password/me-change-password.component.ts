import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-me-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <section class="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-3xl font-bold text-neutral-900">Change password</h1>
            <p class="text-neutral-600 mt-1">Enter your current and new password</p>
          </div>
          <a routerLink="/me" class="btn-secondary">Back</a>
        </div>

        <div class="card p-6">
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-neutral-700 mb-2"
                >Current password</label
              >
              <input
                type="password"
                formControlName="currentPassword"
                class="input-field"
                placeholder="••••••••"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-neutral-700 mb-2">New password</label>
              <input
                type="password"
                formControlName="newPassword"
                class="input-field"
                placeholder="Min. 6 characters"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-neutral-700 mb-2"
                >Confirm password</label
              >
              <input
                type="password"
                formControlName="confirmPassword"
                class="input-field"
                placeholder="Repeat the password"
              />
            </div>

            <div class="flex items-center justify-end gap-3">
              <a routerLink="/me" class="btn-secondary">Cancel</a>
              <button class="btn-primary" type="submit" [disabled]="form.invalid || loading">
                Save
              </button>
            </div>
          </form>
        </div>
      </section>
    </div>
  `,
  styles: [],
})
export class MeChangePasswordComponent {
  form: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private toastr: ToastrService,
    private router: Router
  ) {
    this.form = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const { currentPassword, newPassword, confirmPassword } = this.form.value;
    if (newPassword !== confirmPassword) {
      this.toastr.warning('Passwords do not match');
      return;
    }

    this.loading = true;
    this.userService.changePassword(currentPassword, newPassword, confirmPassword).subscribe({
      next: (res) => {
        this.toastr.success(res.message || 'Password changed successfully');
        this.router.navigate(['/me']);
      },
      error: (err) => this.toastr.error(err?.error?.message || 'Failed to change password'),
      complete: () => (this.loading = false),
    });
  }
}
