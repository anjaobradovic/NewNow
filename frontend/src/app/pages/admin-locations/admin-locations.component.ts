import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { LocationService } from '../../services/location.service';
import { LocationDTO } from '../../models/location.model';

@Component({
  selector: 'app-admin-locations',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-8 px-4 sm:px-6 lg:px-8">
      <div class="max-w-7xl mx-auto">
        <!-- Header -->
        <div class="mb-8 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 class="text-3xl font-bold text-neutral-900 mb-2">Manage Locations</h1>
            <p class="text-neutral-600">Create, edit, and manage venue locations</p>
          </div>
          <button
            (click)="openCreateModal()"
            class="px-6 py-3 bg-primary-600 text-white rounded-2xl font-medium hover:bg-primary-700 transition-all duration-200 shadow-sm hover:shadow-md active:scale-95 flex items-center gap-2 justify-center sm:justify-start"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 4v16m8-8H4"
              />
            </svg>
            <span>Add Location</span>
          </button>
        </div>

        <!-- Search Bar -->
        <div class="bg-white rounded-2xl shadow-sm p-6 mb-6 border border-neutral-100">
          <div class="relative">
            <svg
              class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
            <input
              type="text"
              [(ngModel)]="searchQuery"
              (ngModelChange)="onSearchChange()"
              placeholder="Search locations by name, address, or type..."
              class="w-full pl-12 pr-4 py-3 border-2 border-neutral-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-primary-400 transition-all"
            />
          </div>
        </div>

        <!-- Locations Grid -->
        @if (loading()) {
        <div class="flex items-center justify-center py-20">
          <div class="text-center">
            <div
              class="inline-block w-12 h-12 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin"
            ></div>
            <p class="mt-4 text-neutral-600">Loading locations...</p>
          </div>
        </div>
        } @else if (locations.length > 0) {
        <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          @for (location of locations; track location.id) {
          <div
            class="bg-white rounded-2xl shadow-sm hover:shadow-md transition-all duration-200 overflow-hidden border border-neutral-100 group"
          >
            <!-- Image -->
            <div class="relative h-48 bg-neutral-100 overflow-hidden">
              @if (location.imageUrl) {
              <img
                [src]="location.imageUrl"
                [alt]="location.name"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              />
              } @else {
              <div
                class="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-100 to-primary-200"
              >
                <svg
                  class="w-16 h-16 text-primary-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                  />
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
              </div>
              }
              <div class="absolute top-3 right-3">
                <span
                  class="px-3 py-1 bg-white/90 backdrop-blur-sm text-neutral-900 rounded-full text-xs font-medium shadow-sm"
                >
                  {{ location.type }}
                </span>
              </div>
            </div>

            <!-- Content -->
            <div class="p-6">
              <h3 class="text-lg font-semibold text-neutral-900 mb-2">{{ location.name }}</h3>

              <div class="space-y-2 mb-4">
                <div class="flex items-start gap-2 text-sm text-neutral-600">
                  <svg
                    class="w-4 h-4 text-neutral-400 mt-0.5 flex-shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                    />
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                  </svg>
                  <span>{{ location.address }}</span>
                </div>

                @if (location.totalRating) {
                <div class="flex items-center gap-2 text-sm">
                  <svg class="w-4 h-4 text-yellow-400 fill-current" viewBox="0 0 20 20">
                    <path
                      d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
                    />
                  </svg>
                  <span class="font-medium text-neutral-900">{{
                    location.totalRating.toFixed(1)
                  }}</span>
                </div>
                }
              </div>

              @if (location.description) {
              <p class="text-sm text-neutral-600 mb-4 line-clamp-2">{{ location.description }}</p>
              }

              <!-- Actions -->
              <div class="flex gap-2 pt-4 border-t border-neutral-100">
                <button
                  [routerLink]="['/locations', location.id]"
                  class="flex-1 px-4 py-2 bg-neutral-100 text-neutral-700 rounded-xl font-medium hover:bg-neutral-200 transition-all duration-200 text-sm active:scale-95"
                >
                  View
                </button>
                <button
                  (click)="openEditModal(location)"
                  class="flex-1 px-4 py-2 bg-primary-50 text-primary-700 rounded-xl font-medium hover:bg-primary-100 transition-all duration-200 text-sm active:scale-95"
                >
                  Edit
                </button>
                <button
                  (click)="confirmDelete(location)"
                  class="px-4 py-2 bg-red-50 text-red-600 rounded-xl font-medium hover:bg-red-100 transition-all duration-200 text-sm active:scale-95"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                    />
                  </svg>
                </button>
              </div>
            </div>
          </div>
          }
        </div>

        <!-- Pagination -->
        @if (totalPages > 1) {
        <div class="mt-8 flex justify-center gap-2">
          <button
            (click)="previousPage()"
            [disabled]="currentPage === 0"
            class="px-4 py-2 bg-white border-2 border-neutral-200 text-neutral-700 rounded-xl font-medium hover:bg-neutral-50 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Previous
          </button>
          <span class="px-4 py-2 flex items-center text-neutral-600">
            Page {{ currentPage + 1 }} of {{ totalPages }}
          </span>
          <button
            (click)="nextPage()"
            [disabled]="currentPage >= totalPages - 1"
            class="px-4 py-2 bg-white border-2 border-neutral-200 text-neutral-700 rounded-xl font-medium hover:bg-neutral-50 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Next
          </button>
        </div>
        } } @else {
        <div class="bg-white rounded-2xl shadow-sm p-12 text-center border border-neutral-100">
          <svg
            class="w-16 h-16 mx-auto text-neutral-300 mb-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
            />
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
            />
          </svg>
          <h3 class="text-lg font-semibold text-neutral-900 mb-2">No Locations Found</h3>
          <p class="text-neutral-600 mb-4">
            {{ searchQuery ? 'Try adjusting your search' : 'Start by adding your first location' }}
          </p>
          <button
            (click)="openCreateModal()"
            class="px-6 py-3 bg-primary-600 text-white rounded-2xl font-medium hover:bg-primary-700 transition-all duration-200 shadow-sm inline-flex items-center gap-2"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 4v16m8-8H4"
              />
            </svg>
            <span>Add Location</span>
          </button>
        </div>
        }
      </div>
    </div>

    <!-- Create/Edit Modal -->
    @if (showModal()) {
    <div
      class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-fade-in overflow-y-auto"
      (click)="closeModal()"
    >
      <div
        class="bg-white rounded-3xl shadow-2xl max-w-2xl w-full overflow-hidden animate-slide-up my-8"
        (click)="$event.stopPropagation()"
      >
        <!-- Modal Header -->
        <div class="p-6 border-b border-neutral-100">
          <div class="flex items-center justify-between">
            <h2 class="text-2xl font-bold text-neutral-900">
              {{ isEditMode() ? 'Edit Location' : 'Add New Location' }}
            </h2>
            <button
              (click)="closeModal()"
              class="w-8 h-8 rounded-xl hover:bg-neutral-100 transition-colors flex items-center justify-center"
            >
              <svg
                class="w-5 h-5 text-neutral-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
        </div>

        <!-- Modal Body -->
        <div class="p-6 space-y-6 max-h-[calc(100vh-16rem)] overflow-y-auto">
          <!-- Name -->
          <div>
            <label for="name" class="block text-sm font-medium text-neutral-700 mb-2"
              >Location Name <span class="text-red-500">*</span></label
            >
            <input
              id="name"
              type="text"
              [(ngModel)]="formData.name"
              placeholder="Enter location name"
              class="w-full px-4 py-3 border-2 border-neutral-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-primary-400 transition-all"
            />
          </div>

          <!-- Address -->
          <div>
            <label for="address" class="block text-sm font-medium text-neutral-700 mb-2"
              >Address <span class="text-red-500">*</span></label
            >
            <input
              id="address"
              type="text"
              [(ngModel)]="formData.address"
              placeholder="Enter full address"
              class="w-full px-4 py-3 border-2 border-neutral-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-primary-400 transition-all"
            />
          </div>

          <!-- Type -->
          <div>
            <label for="type" class="block text-sm font-medium text-neutral-700 mb-2"
              >Venue Type <span class="text-red-500">*</span></label
            >
            <select
              id="type"
              [(ngModel)]="formData.type"
              class="w-full px-4 py-3 border-2 border-neutral-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-primary-400 transition-all"
            >
              <option value="">Select venue type</option>
              <option value="Club">Club</option>
              <option value="Bar">Bar</option>
              <option value="Restaurant">Restaurant</option>
              <option value="Cafe">Cafe</option>
              <option value="Concert Hall">Concert Hall</option>
              <option value="Theater">Theater</option>
              <option value="Gallery">Gallery</option>
              <option value="Other">Other</option>
            </select>
          </div>

          <!-- Description -->
          <div>
            <label for="description" class="block text-sm font-medium text-neutral-700 mb-2"
              >Description</label
            >
            <textarea
              id="description"
              [(ngModel)]="formData.description"
              rows="4"
              placeholder="Describe the location..."
              class="w-full px-4 py-3 border-2 border-neutral-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-primary-400 transition-all resize-none"
            ></textarea>
          </div>

          <!-- Image Upload -->
          <div>
            <label class="block text-sm font-medium text-neutral-700 mb-2">
              Location Image {{ !isEditMode() ? '*' : '(optional)' }}
            </label>
            <div
              class="border-2 border-dashed border-neutral-300 rounded-xl p-6 text-center hover:border-primary-400 transition-colors cursor-pointer"
              (click)="fileInput.click()"
            >
              @if (imagePreview()) {
              <div class="relative">
                <img [src]="imagePreview()" alt="Preview" class="max-h-48 mx-auto rounded-lg" />
                <button
                  (click)="clearImage($event)"
                  class="absolute top-2 right-2 w-8 h-8 bg-red-500 text-white rounded-full hover:bg-red-600 transition-colors"
                >
                  ×
                </button>
              </div>
              } @else {
              <svg
                class="w-12 h-12 mx-auto text-neutral-400 mb-3"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
              <p class="text-sm text-neutral-600 mb-1">Click to upload an image</p>
              <p class="text-xs text-neutral-500">PNG, JPG up to 15MB</p>
              }
            </div>
            <input
              #fileInput
              type="file"
              accept="image/*"
              (change)="onFileSelected($event)"
              class="hidden"
            />
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 bg-neutral-50 border-t border-neutral-100 flex gap-3">
          <button
            (click)="closeModal()"
            [disabled]="isProcessing()"
            class="flex-1 px-6 py-3 bg-white text-neutral-700 border-2 border-neutral-200 rounded-xl font-medium hover:bg-neutral-50 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Cancel
          </button>
          <button
            (click)="submitForm()"
            [disabled]="isProcessing() || !isFormValid()"
            class="flex-1 px-6 py-3 bg-primary-600 text-white rounded-xl font-medium hover:bg-primary-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed active:scale-95"
          >
            {{
              isProcessing() ? 'Saving...' : isEditMode() ? 'Update Location' : 'Create Location'
            }}
          </button>
        </div>
      </div>
    </div>
    }

    <!-- Delete Confirmation Modal -->
    @if (showDeleteModal()) {
    <div
      class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-fade-in"
      (click)="closeDeleteModal()"
    >
      <div
        class="bg-white rounded-3xl shadow-2xl max-w-md w-full overflow-hidden animate-slide-up"
        (click)="$event.stopPropagation()"
      >
        <!-- Modal Header -->
        <div class="p-6 border-b border-neutral-100">
          <h2 class="text-2xl font-bold text-neutral-900">Delete Location</h2>
        </div>

        <!-- Modal Body -->
        @if (locationToDelete()) {
        <div class="p-6">
          <div class="flex items-center gap-4 p-4 bg-red-50 border border-red-100 rounded-xl mb-4">
            <svg
              class="w-6 h-6 text-red-600 flex-shrink-0"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
            <p class="text-sm text-red-900">
              Are you sure you want to delete
              <strong>{{ locationToDelete()!.name }}</strong
              >? This action cannot be undone.
            </p>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 bg-neutral-50 border-t border-neutral-100 flex gap-3">
          <button
            (click)="closeDeleteModal()"
            [disabled]="isProcessing()"
            class="flex-1 px-6 py-3 bg-white text-neutral-700 border-2 border-neutral-200 rounded-xl font-medium hover:bg-neutral-50 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Cancel
          </button>
          <button
            (click)="deleteLocation()"
            [disabled]="isProcessing()"
            class="flex-1 px-6 py-3 bg-red-600 text-white rounded-xl font-medium hover:bg-red-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed active:scale-95"
          >
            {{ isProcessing() ? 'Deleting...' : 'Delete Location' }}
          </button>
        </div>
        }
      </div>
    </div>
    }
  `,
  styles: [],
})
export class AdminLocationsComponent implements OnInit {
  private locationService = inject(LocationService);
  private toastr = inject(ToastrService);

  locations: LocationDTO[] = [];
  loading = signal(true);
  searchQuery = '';
  currentPage = 0;
  totalPages = 0;
  pageSize = 12;

  // Modal states
  showModal = signal(false);
  showDeleteModal = signal(false);
  isEditMode = signal(false);
  isProcessing = signal(false);
  selectedLocation = signal<LocationDTO | null>(null);
  locationToDelete = signal<LocationDTO | null>(null);
  imagePreview = signal<string>('');
  selectedFile: File | null = null;

  // Form data
  formData = {
    name: '',
    address: '',
    type: '',
    description: '',
  };

  private searchTimeout: any;

  ngOnInit() {
    this.loadLocations();
  }

  loadLocations() {
    this.loading.set(true);
    this.locationService.getLocations(this.searchQuery, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.locations = response.locations || [];
        this.totalPages = response.totalPages || 0;
        this.loading.set(false);
      },
      error: (error: any) => {
        const errorMsg = error?.error?.message || error.message || 'Failed to load locations';
        this.toastr.error(errorMsg, 'Error');
        this.loading.set(false);
      },
    });
  }

  onSearchChange() {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.currentPage = 0;
      this.loadLocations();
    }, 500);
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadLocations();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadLocations();
    }
  }

  openCreateModal() {
    this.isEditMode.set(false);
    this.selectedLocation.set(null);
    this.resetForm();
    this.showModal.set(true);
  }

  openEditModal(location: LocationDTO) {
    this.isEditMode.set(true);
    this.selectedLocation.set(location);
    this.formData = {
      name: location.name,
      address: location.address,
      type: location.type,
      description: location.description || '',
    };
    if (location.imageUrl) {
      this.imagePreview.set(location.imageUrl);
    }
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
    this.resetForm();
  }

  resetForm() {
    this.formData = {
      name: '',
      address: '',
      type: '',
      description: '',
    };
    this.selectedFile = null;
    this.imagePreview.set('');
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      // Validate file size (15MB)
      if (file.size > 15 * 1024 * 1024) {
        this.toastr.error('Image size must be less than 15MB', 'Error');
        return;
      }

      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreview.set(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  clearImage(event: Event) {
    event.stopPropagation();
    this.selectedFile = null;
    this.imagePreview.set('');
  }

  isFormValid(): boolean {
    const hasRequiredFields = !!(
      this.formData.name.trim() &&
      this.formData.address.trim() &&
      this.formData.type
    );

    if (this.isEditMode()) {
      return hasRequiredFields;
    }

    return hasRequiredFields && !!this.selectedFile;
  }

  submitForm() {
    if (!this.isFormValid()) return;

    this.isProcessing.set(true);

    if (this.isEditMode()) {
      this.updateLocation();
    } else {
      this.createLocation();
    }
  }

  createLocation() {
    if (!this.selectedFile) return;

    const payload = {
      name: this.formData.name.trim(),
      address: this.formData.address.trim(),
      type: this.formData.type,
      description: this.formData.description.trim(),
      image: this.selectedFile,
    };

    this.locationService.createLocation(payload).subscribe({
      next: () => {
        this.toastr.success('Location created successfully', 'Success');
        this.closeModal();
        this.loadLocations();
        this.isProcessing.set(false);
      },
      error: (error: any) => {
        const errorMsg = error?.error?.message || error.message || 'Failed to create location';
        this.toastr.error(errorMsg, 'Error');
        this.isProcessing.set(false);
      },
    });
  }

  updateLocation() {
    const location = this.selectedLocation();
    if (!location) return;

    const patchData = {
      address: this.formData.address.trim(),
      type: this.formData.type,
      description: this.formData.description.trim(),
    };

    this.locationService.patchLocation(location.id, patchData).subscribe({
      next: () => {
        // If there's a new image, update it separately
        if (this.selectedFile) {
          this.updateLocationImage(location.id);
        } else {
          this.toastr.success('Location updated successfully', 'Success');
          this.closeModal();
          this.loadLocations();
          this.isProcessing.set(false);
        }
      },
      error: (error: any) => {
        const errorMsg = error?.error?.message || error.message || 'Failed to update location';
        this.toastr.error(errorMsg, 'Error');
        this.isProcessing.set(false);
      },
    });
  }

  updateLocationImage(locationId: number) {
    if (!this.selectedFile) return;

    this.locationService.updateLocationImage(locationId, this.selectedFile).subscribe({
      next: () => {
        this.toastr.success('Location updated successfully', 'Success');
        this.closeModal();
        this.loadLocations();
        this.isProcessing.set(false);
      },
      error: (error: any) => {
        const errorMsg = error?.error?.message || error.message || 'Failed to update image';
        this.toastr.error(errorMsg, 'Error');
        this.isProcessing.set(false);
      },
    });
  }

  confirmDelete(location: LocationDTO) {
    this.locationToDelete.set(location);
    this.showDeleteModal.set(true);
  }

  closeDeleteModal() {
    this.showDeleteModal.set(false);
    this.locationToDelete.set(null);
  }

  deleteLocation() {
    const location = this.locationToDelete();
    if (!location) return;

    this.isProcessing.set(true);

    this.locationService.deleteLocation(location.id).subscribe({
      next: () => {
        this.toastr.success('Location deleted successfully', 'Success');
        this.closeDeleteModal();
        this.loadLocations();
        this.isProcessing.set(false);
      },
      error: (error: any) => {
        const errorMsg = error?.error?.message || error.message || 'Failed to delete location';
        this.toastr.error(errorMsg, 'Error');
        this.isProcessing.set(false);
      },
    });
  }
}
