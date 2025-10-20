import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AccountRequestService } from '../../services/account-request.service';
import { AccountRequest, RequestStatus } from '../../models/account-request.model';

@Component({
  selector: 'app-admin-requests',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-requests.component.html',
  styleUrls: ['./admin-requests.component.scss'],
})
export class AdminRequestsComponent implements OnInit {
  private accountRequestService = inject(AccountRequestService);
  private toastr: ToastrService = inject(ToastrService);

  requests: AccountRequest[] = [];
  filteredRequests: AccountRequest[] = [];
  selectedRequest: AccountRequest | null = null;
  showModal = false;
  isProcessing = false;
  rejectionReason = '';
  filterStatus: string = 'PENDING';

  RequestStatus = RequestStatus;

  ngOnInit() {
    this.loadRequests();
  }

  loadRequests() {
    const statusParam = this.filterStatus === 'ALL' ? undefined : this.filterStatus.toLowerCase();

    if (statusParam === 'pending') {
      this.accountRequestService.getPendingRequests().subscribe({
        next: (res: any) => {
          const content = Array.isArray(res?.content) ? res.content : res || [];
          this.requests = content;
          this.filteredRequests = content;
        },
        error: (error: any) => {
          const errorMsg =
            error?.error?.message || error.message || 'Failed to load pending requests';
          this.toastr.error(errorMsg, 'Error');
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
        },
        error: (error: any) => {
          const errorMsg = error?.error?.message || error.message || 'Failed to load requests';
          this.toastr.error(errorMsg, 'Error');
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
    this.selectedRequest = request;
    this.rejectionReason = '';
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.selectedRequest = null;
    this.rejectionReason = '';
  }

  approveRequest(request: AccountRequest) {
    this.isProcessing = true;

    this.accountRequestService.approveRequest(request.id).subscribe({
      next: () => {
        this.toastr.success(`Registration approved for ${request.name}. Email sent.`, 'Success');
        this.loadRequests();
        this.isProcessing = false;
      },
      error: (error: any) => {
        const errorMsg = error?.error?.message || error.message || 'Failed to approve request';
        this.toastr.error(errorMsg, 'Approval Failed');
        this.isProcessing = false;
      },
    });
  }

  rejectRequest() {
    if (!this.selectedRequest) return;

    if (!this.rejectionReason.trim()) {
      this.toastr.warning('Please provide a rejection reason', 'Validation Error');
      return;
    }

    this.isProcessing = true;

    this.accountRequestService.rejectRequest(this.selectedRequest.id).subscribe({
      next: () => {
        this.toastr.success(
          `Registration rejected for ${this.selectedRequest?.name}. Email sent.`,
          'Success'
        );
        this.closeModal();
        this.loadRequests();
        this.isProcessing = false;
      },
      error: (error: any) => {
        const errorMsg = error?.error?.message || error.message || 'Failed to reject request';
        this.toastr.error(errorMsg, 'Rejection Failed');
        this.isProcessing = false;
      },
    });
  }

  getStatusBadgeClass(status: RequestStatus): string {
    switch (status) {
      case RequestStatus.PENDING:
        return 'badge-warning';
      case RequestStatus.ACCEPTED:
        return 'badge-success';
      case RequestStatus.REJECTED:
        return 'badge-danger';
      default:
        return 'badge-secondary';
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('sr-RS');
  }
}
