import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
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
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('name') }}</p>
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
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('email') }}</p>
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
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('password') }}</p>
                } @if (!registerForm.get('password')?.touched) {
                <p class="mt-2 text-xs text-neutral-500">
                  Min 8 characters, with uppercase, lowercase, number & special character
                </p>
                }
              </div>

              <!-- Phone -->
              <div>
                <label for="phoneNumber" class="block text-sm font-medium text-neutral-700 mb-2">
                  Phone Number *
                </label>
                <input
                  id="phoneNumber"
                  type="tel"
                  formControlName="phoneNumber"
                  class="input-field"
                  [class.border-red-400]="
                    registerForm.get('phoneNumber')?.invalid &&
                    registerForm.get('phoneNumber')?.touched
                  "
                  placeholder="+381 64 123 4567"
                />
                @if (registerForm.get('phoneNumber')?.invalid &&
                registerForm.get('phoneNumber')?.touched) {
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('phoneNumber') }}</p>
                } @if (!registerForm.get('phoneNumber')?.touched) {
                <p class="mt-2 text-xs text-neutral-500">
                  Format: +381XXXXXXXXX or 06XXXXXXXX (8-15 digits)
                </p>
                }
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
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('address') }}</p>
                }
              </div>

              <!-- City -->
              <div>
                <label for="city" class="block text-sm font-medium text-neutral-700 mb-2">
                  City *
                </label>
                <input
                  id="city"
                  type="text"
                  formControlName="city"
                  class="input-field"
                  [class.border-red-400]="
                    registerForm.get('city')?.invalid && registerForm.get('city')?.touched
                  "
                  placeholder="Novi Sad"
                />
                @if (registerForm.get('city')?.invalid && registerForm.get('city')?.touched) {
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('city') }}</p>
                }
              </div>
            </div>

            <!-- Birthday -->
            <div>
              <label for="birthday" class="block text-sm font-medium text-neutral-700 mb-2">
                Birthday *
              </label>
              <input
                id="birthday"
                type="date"
                formControlName="birthday"
                class="input-field"
                [class.border-red-400]="
                  registerForm.get('birthday')?.invalid && registerForm.get('birthday')?.touched
                "
                [max]="getMaxDate()"
              />
              @if (registerForm.get('birthday')?.invalid && registerForm.get('birthday')?.touched) {
              <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('birthday') }}</p>
              } @if (!registerForm.get('birthday')?.touched) {
              <p class="mt-2 text-xs text-neutral-500">
                You must be at least 13 years old to register
              </p>
              }
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
            <a
              routerLink="/auth/login"
              class="font-medium text-primary-600 hover:text-primary-700 ml-1"
            >
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
      password: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
      name: ['', [Validators.required, Validators.minLength(2), this.nameValidator]],
      phoneNumber: ['', [Validators.required, this.phoneValidator]],
      birthday: ['', [Validators.required, this.ageValidator]],
      address: ['', [Validators.required, Validators.minLength(5)]],
      city: ['', [Validators.required, Validators.minLength(2), this.nameValidator]],
    });
  }

  // Custom validator za ime i prezime - samo slova, razmaci i neki specijalni karakteri
  nameValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // Dozvoljavamo prazno polje ako nije required
    }
    const nameRegex = /^[a-zA-ZčćžšđČĆŽŠĐ\s'-]+$/;
    return nameRegex.test(control.value) ? null : { invalidName: true };
  }

  // Custom validator za lozinku - mora sadržati: veliko slovo, malo slovo, broj i specijalni karakter
  passwordValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null;
    }

    const hasUpperCase = /[A-Z]/.test(control.value);
    const hasLowerCase = /[a-z]/.test(control.value);
    const hasNumber = /\d/.test(control.value);
    const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(control.value);

    const errors: ValidationErrors = {};

    if (!hasUpperCase) errors['noUpperCase'] = true;
    if (!hasLowerCase) errors['noLowerCase'] = true;
    if (!hasNumber) errors['noNumber'] = true;
    if (!hasSpecialChar) errors['noSpecialChar'] = true;

    return Object.keys(errors).length > 0 ? errors : null;
  }

  // Custom validator za telefon - striktna provera
  phoneValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value || control.value.trim() === '') {
      return null; // Required validator će to uhvatiti
    }

    const phoneValue = control.value.replace(/[\s\-\(\)]/g, ''); // Ukloni razmake, crtice i zagrade

    // Provera da li sadrži samo brojeve i opciono + na početku
    if (!/^[\+]?[0-9]+$/.test(phoneValue)) {
      return { invalidPhone: true };
    }

    // Provera dužine (bez +)
    const digitsOnly = phoneValue.replace('+', '');

    // Minimalno 8 cifara (lokalni brojevi), maksimalno 15 (međunarodni standard)
    if (digitsOnly.length < 8 || digitsOnly.length > 15) {
      return { invalidPhoneLength: true };
    }

    // Provera validnih prefiksa za Srbiju (+381 ili 0)
    if (phoneValue.startsWith('+381')) {
      // Nakon +381 mora biti 6, 7 ili 9 (mobilni), ili drugo za fiksne
      const afterPrefix = phoneValue.substring(4);
      if (afterPrefix.length < 8 || afterPrefix.length > 9) {
        return { invalidPhone: true };
      }
    } else if (phoneValue.startsWith('381')) {
      // Isto kao gore ali bez +
      const afterPrefix = phoneValue.substring(3);
      if (afterPrefix.length < 8 || afterPrefix.length > 9) {
        return { invalidPhone: true };
      }
    } else if (phoneValue.startsWith('0')) {
      // Lokalni format za Srbiju - 0XX YYY ZZZZ (10 cifara)
      if (digitsOnly.length < 9 || digitsOnly.length > 10) {
        return { invalidPhone: true };
      }
    }

    return null;
  }

  // Custom validator za starost (13-120 godina)
  ageValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // Datum nije obavezan
    }

    const birthday = new Date(control.value);
    const today = new Date();
    const age = today.getFullYear() - birthday.getFullYear();
    const monthDiff = today.getMonth() - birthday.getMonth();

    const actualAge =
      monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthday.getDate()) ? age - 1 : age;

    if (actualAge < 13) {
      return { tooYoung: true };
    }
    if (actualAge > 120) {
      return { tooOld: true };
    }
    if (birthday > today) {
      return { futureDate: true };
    }

    return null;
  }

  getErrorMessage(fieldName: string): string {
    const control = this.registerForm.get(fieldName);
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    const errors = control.errors;

    switch (fieldName) {
      case 'name':
        if (errors['required']) return 'Name is required';
        if (errors['minlength']) return 'Name must be at least 2 characters';
        if (errors['invalidName']) return 'Name can only contain letters, spaces, and hyphens';
        break;

      case 'email':
        if (errors['required']) return 'Email is required';
        if (errors['email']) return 'Please enter a valid email address';
        break;

      case 'password':
        if (errors['required']) return 'Password is required';
        if (errors['minlength']) return 'Password must be at least 8 characters';
        if (errors['noUpperCase']) return 'Password must contain at least one uppercase letter';
        if (errors['noLowerCase']) return 'Password must contain at least one lowercase letter';
        if (errors['noNumber']) return 'Password must contain at least one number';
        if (errors['noSpecialChar']) return 'Password must contain at least one special character';
        break;

      case 'phoneNumber':
        if (errors['required']) return 'Phone number is required';
        if (errors['invalidPhone']) return 'Please enter a valid phone number';
        if (errors['invalidPhoneLength']) return 'Phone number must be between 8 and 15 digits';
        break;

      case 'address':
        if (errors['required']) return 'Address is required';
        if (errors['minlength']) return 'Address must be at least 5 characters';
        break;

      case 'city':
        if (errors['required']) return 'City is required';
        if (errors['minlength']) return 'City must be at least 2 characters';
        if (errors['invalidName']) return 'City can only contain letters and spaces';
        break;

      case 'birthday':
        if (errors['required']) return 'Birthday is required';
        if (errors['tooYoung']) return 'You must be at least 13 years old';
        if (errors['tooOld']) return 'Please enter a valid birth date';
        if (errors['futureDate']) return 'Birth date cannot be in the future';
        break;
    }

    return 'Invalid input';
  }

  getMaxDate(): string {
    // Maksimalni datum je danas (ne možeš biti rođen u budućnosti)
    const today = new Date();
    return today.toISOString().split('T')[0];
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.isLoading = true;

      const formValue = this.registerForm.value;
      const requestData: any = {
        email: formValue.email,
        password: formValue.password,
        name: formValue.name,
        address: formValue.address,
        phoneNumber: formValue.phoneNumber,
        birthday: formValue.birthday,
        city: formValue.city,
      };

      this.authService.register(requestData).subscribe({
        next: (res) => {
          this.toastr.success(res.message || 'Registration request submitted');
          this.router.navigate(['/auth/login']);
        },
        error: (error) => {
          const errorMsg =
            typeof error.error === 'string'
              ? error.error
              : error.error?.message || error.message || 'Registration failed';
          this.toastr.error(errorMsg, 'Error');
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        },
      });
    } else {
      Object.keys(this.registerForm.controls).forEach((key) => {
        this.registerForm.get(key)?.markAsTouched();
      });
      this.toastr.warning('Please fill in all required fields', 'Validation Error');
    }
  }
}
