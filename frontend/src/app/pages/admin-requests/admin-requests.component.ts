import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AccountRequestService } from '../../services/account-request.service';
import { AccountRequest, RequestStatus } from '../../models/account-request.model';

@Component({
  selector: 'app-admin-requests',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen bg-neutral-50 py-8 px-4 sm:px-6 lg:px-8">
      <div class="max-w-7xl mx-auto">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-neutral-900 mb-2">Registration Requests</h1>
          <p class="text-neutral-600">Review and manage account registration requests</p>
        </div>

        <!-- Filter Controls -->
        <div class="bg-white rounded-2xl shadow-sm p-6 mb-6 border border-neutral-100">
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div class="flex items-center gap-3">
              <label for="statusFilter" class="text-sm font-medium text-neutral-700">Filter:</label>
              <select
                id="statusFilter"
                [(ngModel)]="filterStatus"
                (change)="onFilterChange()"
                class="px-4 py-2 border-2 border-neutral-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-primary-400 transition-all"
              >
                <option value="PENDING">Pending</option>
                <option value="ACCEPTED">Accepted</option>
                <option value="REJECTED">Rejected</option>
                <option value="ALL">All</option>
              </select>
            </div>
            <div class="text-sm text-neutral-600">
              Showing <span class="font-semibold">{{ filteredRequests.length }}</span> request{{
                filteredRequests.length !== 1 ? 's' : ''
              }}
            </div>
          </div>
        </div>

        <!-- Requests Grid -->
        @if (loading()) {
        <div class="flex items-center justify-center py-20">
          <div class="text-center">
            <div
              class="inline-block w-12 h-12 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin"
            ></div>
            <p class="mt-4 text-neutral-600">Loading requests...</p>
          </div>
        </div>
        } @else if (filteredRequests.length > 0) {
        <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          @for (request of filteredRequests; track request.id) {
          <div
            class="bg-white rounded-2xl shadow-sm hover:shadow-md transition-all duration-200 overflow-hidden border border-neutral-100"
          >
            <!-- Card Header -->
            <div class="p-6 border-b border-neutral-100">
              <div class="flex items-start justify-between mb-3">
                <div class="flex-1">
                  <h3 class="text-lg font-semibold text-neutral-900 mb-1">
                    {{ request.name }}
                  </h3>
                  <p class="text-sm text-neutral-600">{{ request.email }}</p>
                </div>
                <span
                  [class]="
                    'px-3 py-1 rounded-full text-xs font-medium ' +
                    getStatusBadgeClass(request.status)
                  "
                >
                  {{ request.status }}
                </span>
              </div>
            </div>

            <!-- Card Body -->
            <div class="p-6 space-y-3">
              @if (request.phoneNumber) {
              <div class="flex items-center gap-3 text-sm">
                <svg
                  class="w-4 h-4 text-neutral-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"
                  />
                </svg>
                <span class="text-neutral-700">{{ request.phoneNumber }}</span>
              </div>
              } @if (request.address) {
              <div class="flex items-center gap-3 text-sm">
                <svg
                  class="w-4 h-4 text-neutral-400"
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
                <span class="text-neutral-700">{{ request.address }}</span>
              </div>
              } @if (request.birthday) {
              <div class="flex items-center gap-3 text-sm">
                <svg
                  class="w-4 h-4 text-neutral-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
                <span class="text-neutral-700">{{ formatDate(request.birthday) }}</span>
              </div>
              }
              <div class="flex items-center gap-3 text-sm">
                <svg
                  class="w-4 h-4 text-neutral-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                <span class="text-neutral-700">{{ formatDate(request.createdAt) }}</span>
              </div>

              @if (request.status === 'REJECTED' && request.rejectionReason) {
              <div class="mt-4 p-3 bg-red-50 border border-red-100 rounded-xl">
                <p class="text-xs font-medium text-red-900 mb-1">Rejection Reason:</p>
                <p class="text-sm text-red-700">{{ request.rejectionReason }}</p>
              </div>
              }
            </div>

            <!-- Card Actions -->
            @if (request.status === 'PENDING') {
            <div class="p-6 bg-neutral-50 border-t border-neutral-100 flex gap-3">
              <button
                (click)="approveRequest(request)"
                [disabled]="isProcessing()"
                class="flex-1 px-4 py-2 bg-primary-600 text-white rounded-xl font-medium hover:bg-primary-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed active:scale-95"
              >
                <svg
                  class="w-4 h-4 inline-block mr-1"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M5 13l4 4L19 7"
                  />
                </svg>
                Approve
              </button>
              <button
                (click)="openModal(request)"
                [disabled]="isProcessing()"
                class="flex-1 px-4 py-2 bg-white text-red-600 border-2 border-red-200 rounded-xl font-medium hover:bg-red-50 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed active:scale-95"
              >
                <svg
                  class="w-4 h-4 inline-block mr-1"
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
                Reject
              </button>
            </div>
            }
          </div>
          }
        </div>
        } @else {
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
              d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
            />
          </svg>
          <h3 class="text-lg font-semibold text-neutral-900 mb-2">No Requests Found</h3>
          <p class="text-neutral-600">
            No {{ filterStatus === 'ALL' ? '' : filterStatus.toLowerCase() }} requests at this time
          </p>
        </div>
        }
      </div>
    </div>

    <!-- Rejection Modal -->
    @if (showModal()) {
    <div
      class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-fade-in"
      (click)="closeModal()"
    >
      <div
        class="bg-white rounded-3xl shadow-2xl max-w-lg w-full overflow-hidden animate-slide-up"
        (click)="$event.stopPropagation()"
      >
        <!-- Modal Header -->
        <div class="p-6 border-b border-neutral-100">
          <div class="flex items-center justify-between">
            <h2 class="text-2xl font-bold text-neutral-900">Reject Request</h2>
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
        @if (selectedRequest()) {
        <div class="p-6 space-y-4">
          <div class="p-4 bg-red-50 border border-red-100 rounded-xl">
            <p class="text-sm text-red-900">
              You are about to reject the registration request for
              <strong>{{ selectedRequest()!.name }}</strong> ({{ selectedRequest()!.email }})
            </p>
          </div>

          <div>
            <label for="rejectionReason" class="block text-sm font-medium text-neutral-700 mb-2"
              >Rejection Reason <span class="text-red-500">*</span></label
            >
            <textarea
              id="rejectionReason"
              [(ngModel)]="rejectionReason"
              rows="4"
              placeholder="Please provide a clear reason for rejection..."
              class="w-full px-4 py-3 border-2 border-neutral-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-400 focus:border-primary-400 transition-all resize-none"
            ></textarea>
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
            (click)="rejectRequest()"
            [disabled]="isProcessing() || !rejectionReason.trim()"
            class="flex-1 px-6 py-3 bg-red-600 text-white rounded-xl font-medium hover:bg-red-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed active:scale-95"
          >
            {{ isProcessing() ? 'Processing...' : 'Reject Request' }}
          </button>
        </div>
        }
      </div>
    </div>
    }
  `,
  styles: [],
})
export class AdminRequestsComponent implements OnInit {
  private accountRequestService = inject(AccountRequestService);
  private toastr = inject(ToastrService);

  requests: AccountRequest[] = [];
  filteredRequests: AccountRequest[] = [];
  selectedRequest = signal<AccountRequest | null>(null);
  showModal = signal(false);
  isProcessing = signal(false);
  loading = signal(true);
  rejectionReason = '';
  filterStatus = 'PENDING';

  RequestStatus = RequestStatus;

  ngOnInit() {
    this.loadRequests();
  }

  loadRequests() {
    this.loading.set(true);
    const statusParam = this.filterStatus === 'ALL' ? undefined : this.filterStatus.toLowerCase();

    if (statusParam === 'pending') {
      this.accountRequestService.getPendingRequests().subscribe({
        next: (res: any) => {
          const content = Array.isArray(res?.content) ? res.content : res || [];
          this.requests = content;
          this.filteredRequests = content;
          this.loading.set(false);
        },
        error: (error: any) => {
          const errorMsg =
            error?.error?.message || error.message || 'Failed to load pending requests';
          this.toastr.error(errorMsg, 'Error');
          this.loading.set(false);
        },
      });
    } else {
      this.accountRequestService.getAllRequests(statusParam).subscribe({
        next: (res) => {
          const content = Array.isArray((res as any)?.content)
            ? (res as any).content
            : (res as any) || [];
          this.requests = content;
          this.applyFilter();
          this.loading.set(false);
        },
        error: (error: any) => {
          const errorMsg = error?.error?.message || error.message || 'Failed to load requests';
          this.toastr.error(errorMsg, 'Error');
          this.loading.set(false);
        },
      });
    }
  }

  applyFilter() {
    if (this.filterStatus === 'ALL') {
      this.filteredRequests = this.requests;
    } else {
      this.filteredRequests = this.requests.filter((req) => req.status === this.filterStatus);
    }
  }

  onFilterChange() {
    this.loadRequests();
  }

  openModal(request: AccountRequest) {
    this.selectedRequest.set(request);
    this.rejectionReason = '';
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
    this.selectedRequest.set(null);
    this.rejectionReason = '';
  }

  approveRequest(request: AccountRequest) {
    this.isProcessing.set(true);

    this.accountRequestService.approveRequest(request.id).subscribe({
      next: () => {
        this.toastr.success(`Registration approved for ${request.name}. Email sent.`, 'Success');
        this.loadRequests();
        this.isProcessing.set(false);
      },
      error: (error: any) => {
        const errorMsg = error?.error?.message || error.message || 'Failed to approve request';
        this.toastr.error(errorMsg, 'Approval Failed');
        this.isProcessing.set(false);
      },
    });
  }

  rejectRequest() {
    const request = this.selectedRequest();
    if (!request) return;

    if (!this.rejectionReason.trim()) {
      this.toastr.warning('Please provide a rejection reason', 'Validation Error');
      return;
    }

    this.isProcessing.set(true);

    this.accountRequestService.rejectRequest(request.id).subscribe({
      next: () => {
        this.toastr.success(`Registration rejected for ${request.name}. Email sent.`, 'Success');
        this.closeModal();
        this.loadRequests();
        this.isProcessing.set(false);
      },
      error: (error: any) => {
        const errorMsg = error?.error?.message || error.message || 'Failed to reject request';
        this.toastr.error(errorMsg, 'Rejection Failed');
        this.isProcessing.set(false);
      },
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 border border-yellow-200';
      case 'ACCEPTED':
        return 'bg-green-100 text-green-800 border border-green-200';
      case 'REJECTED':
        return 'bg-red-100 text-red-800 border border-red-200';
      default:
        return 'bg-neutral-100 text-neutral-800 border border-neutral-200';
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  }
}
