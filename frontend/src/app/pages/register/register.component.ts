import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div
      class="h-screen flex items-center justify-center bg-gradient-to-br from-autumn-cream via-neutral-50 to-autumn-sand px-4 sm:px-6 lg:px-8 overflow-hidden"
    >
      <div class="max-w-2xl w-full space-y-6 animate-slide-up">
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
                d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"
              />
            </svg>
          </div>
          <h2 class="text-3xl font-bold text-neutral-900 mb-2">Create your account</h2>
          <p class="text-neutral-600">Join us to discover and explore amazing events</p>
        </div>

        <!-- Form -->
        <form [formGroup]="registerForm" (ngSubmit)="onSubmit()" class="mt-8">
          <div class="card p-8 space-y-6">
            <!-- Basic Info -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Name -->
              <div>
                <label for="name" class="block text-sm font-medium text-neutral-700 mb-2">
                  Full Name *
                </label>
                <input
                  id="name"
                  type="text"
                  formControlName="name"
                  class="input-field"
                  [class.border-red-400]="
                    registerForm.get('name')?.invalid && registerForm.get('name')?.touched
                  "
                  placeholder="John Doe"
                />
                @if (registerForm.get('name')?.invalid && registerForm.get('name')?.touched) {
                <p class="mt-2 text-sm text-red-600">Name is required</p>
                }
              </div>

              <!-- Email -->
              <div>
                <label for="email" class="block text-sm font-medium text-neutral-700 mb-2">
                  Email address *
                </label>
                <input
                  id="email"
                  type="email"
                  formControlName="email"
                  class="input-field"
                  [class.border-red-400]="
                    registerForm.get('email')?.invalid && registerForm.get('email')?.touched
                  "
                  placeholder="your@email.com"
                />
                @if (registerForm.get('email')?.invalid && registerForm.get('email')?.touched) {
                <p class="mt-2 text-sm text-red-600">Please enter a valid email</p>
                }
              </div>
            </div>

            <!-- Password & Phone -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Password -->
              <div>
                <label for="password" class="block text-sm font-medium text-neutral-700 mb-2">
                  Password *
                </label>
                <input
                  id="password"
                  type="password"
                  formControlName="password"
                  class="input-field"
                  [class.border-red-400]="
                    registerForm.get('password')?.invalid && registerForm.get('password')?.touched
                  "
                  placeholder="••••••••"
                />
                @if (registerForm.get('password')?.invalid && registerForm.get('password')?.touched)
                {
                <p class="mt-2 text-sm text-red-600">Password must be at least 6 characters</p>
                }
              </div>

              <!-- Phone -->
              <div>
                <label for="phoneNumber" class="block text-sm font-medium text-neutral-700 mb-2">
                  Phone Number
                </label>
                <input
                  id="phoneNumber"
                  type="tel"
                  formControlName="phoneNumber"
                  class="input-field"
                  placeholder="+1234567890"
                />
              </div>
            </div>

            <!-- Address & City -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Address -->
              <div>
                <label for="address" class="block text-sm font-medium text-neutral-700 mb-2">
                  Address *
                </label>
                <input
                  id="address"
                  type="text"
                  formControlName="address"
                  class="input-field"
                  [class.border-red-400]="
                    registerForm.get('address')?.invalid && registerForm.get('address')?.touched
                  "
                  placeholder="123 Main St"
                />
                @if (registerForm.get('address')?.invalid && registerForm.get('address')?.touched) {
                <p class="mt-2 text-sm text-red-600">Address is required</p>
                }
              </div>

              <!-- City -->
              <div>
                <label for="city" class="block text-sm font-medium text-neutral-700 mb-2">
                  City
                </label>
                <input
                  id="city"
                  type="text"
                  formControlName="city"
                  class="input-field"
                  placeholder="Novi Sad"
                />
              </div>
            </div>

            <!-- Birthday -->
            <div>
              <label for="birthday" class="block text-sm font-medium text-neutral-700 mb-2">
                Birthday
              </label>
              <input id="birthday" type="date" formControlName="birthday" class="input-field" />
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              [disabled]="registerForm.invalid || isLoading"
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
              <span>Create Account</span>
              }
            </button>

            <div class="bg-blue-50 border border-blue-200 rounded-xl p-4">
              <p class="text-sm text-neutral-700 text-center">
                Your registration will be reviewed by an administrator
              </p>
            </div>
          </div>
        </form>

        <!-- Login Link -->
        <div class="text-center">
          <p class="text-sm text-neutral-600">
            Already have an account?
            <a routerLink="/login" class="font-medium text-primary-600 hover:text-primary-700 ml-1">
              Sign in
            </a>
          </p>
        </div>
      </div>
    </div>
  `,
  styles: [],
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      name: ['', Validators.required],
      phoneNumber: [''],
      birthday: [''],
      address: ['', Validators.required],
      city: [''],
    });
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.isLoading = true;
      this.authService.register(this.registerForm.value).subscribe({
        next: (message) => {
          this.toastr.success(message, 'Registration Submitted');
          this.router.navigate(['/login']);
        },
        error: (error) => {
          this.toastr.error(error.error || 'Registration failed', 'Error');
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        },
      });
    }
  }
}
