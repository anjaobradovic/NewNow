import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div
      class="h-screen flex items-center justify-center bg-gradient-to-br from-autumn-cream via-neutral-50 to-autumn-sand px-4 sm:px-6 lg:px-8 overflow-hidden"
    >
      <div class="max-w-md w-full space-y-8 animate-slide-up">
        <!-- Header -->
        <div class="text-center">
          <div
            class="mx-auto w-16 h-16 bg-gradient-to-br from-primary-400 via-primary-500 to-primary-600 rounded-2xl flex items-center justify-center mb-4 shadow-lg transform hover:scale-105 transition-all duration-200"
          >
            <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"
              />
            </svg>
          </div>
          <h2 class="text-3xl font-bold text-neutral-900 mb-2">Welcome back</h2>
          <p class="text-neutral-600">Sign in to discover amazing events</p>
        </div>

        <!-- Form -->
        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="mt-8 space-y-6">
          <div class="card p-8 space-y-6">
            <!-- Email -->
            <div>
              <label for="email" class="block text-sm font-medium text-neutral-700 mb-2">
                Email address
              </label>
              <input
                id="email"
                type="email"
                formControlName="email"
                class="input-field"
                [class.border-red-400]="
                  loginForm.get('email')?.invalid && loginForm.get('email')?.touched
                "
                placeholder="your@email.com"
              />
              @if (loginForm.get('email')?.invalid && loginForm.get('email')?.touched) {
              <p class="mt-2 text-sm text-red-600">Please enter a valid email</p>
              }
            </div>

            <!-- Password -->
            <div>
              <label for="password" class="block text-sm font-medium text-neutral-700 mb-2">
                Password
              </label>
              <input
                id="password"
                type="password"
                formControlName="password"
                class="input-field"
                [class.border-red-400]="
                  loginForm.get('password')?.invalid && loginForm.get('password')?.touched
                "
                placeholder="••••••••"
              />
              @if (loginForm.get('password')?.invalid && loginForm.get('password')?.touched) {
              <p class="mt-2 text-sm text-red-600">Password is required</p>
              }
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              [disabled]="loginForm.invalid || isLoading"
              class="w-full btn-primary disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
            >
              @if (isLoading) {
              <svg
                class="animate-spin h-5 w-5 text-white"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  class="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  stroke-width="4"
                ></circle>
                <path
                  class="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                ></path>
              </svg>
              } @else {
              <span>Sign in</span>
              }
            </button>
          </div>
        </form>

        <!-- Register Link -->
        <div class="text-center">
          <p class="text-sm text-neutral-600">
            Don't have an account?
            <a
              routerLink="/auth/register-request"
              class="font-medium text-primary-600 hover:text-primary-700 ml-1"
            >
              Register now
            </a>
          </p>
        </div>
      </div>
    </div>
  `,
  styles: [],
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          const currentUser = this.authService.currentUser();
          this.toastr.success('Welcome back');
          if (currentUser?.roles?.includes('ROLE_ADMIN')) {
            this.router.navigate(['/admin']);
          } else {
            this.router.navigate(['/']);
          }
        },
        error: (error) => {
          const msg = typeof error.error === 'string' ? error.error : 'Invalid credentials';
          this.toastr.error(msg, 'Sign in failed');
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        },
      });
    } else {
      this.toastr.warning('Please enter your email and password');
    }
  }
}
