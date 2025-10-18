import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AccountRequestService } from '../../services/account-request.service';
import { EventService } from '../../services/event.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private accountRequestService = inject(AccountRequestService);
  private eventService = inject(EventService);
  private router = inject(Router);

  currentUser = this.authService.currentUser();

  // Real data from API
  pendingRequestsCount = 0;
  totalRequestsCount = 0;
  acceptedRequestsCount = 0;
  rejectedRequestsCount = 0;
  eventsCount = 0;
  locationsCount = 0;

  // Calculated statistics
  approvalRate = 0;

  loading = true;
  hasStatistics = false;

  ngOnInit() {
    this.loadStatistics();
  }

  loadStatistics() {
    this.loading = true;

    forkJoin({
      pendingRequests: this.accountRequestService.getPendingRequests(),
      allRequests: this.accountRequestService.getAllRequests(),
      events: this.eventService.getTodayEvents(),
      locations: this.eventService.getPopularLocations(),
    }).subscribe({
      next: (results) => {
        // Pending requests
        this.pendingRequestsCount = results.pendingRequests.length;

        // All requests
        this.totalRequestsCount = results.allRequests.length;
        this.acceptedRequestsCount = results.allRequests.filter(
          (r) => r.status === 'ACCEPTED'
        ).length;
        this.rejectedRequestsCount = results.allRequests.filter(
          (r) => r.status === 'REJECTED'
        ).length;

        // Calculate approval rate
        if (this.totalRequestsCount > 0) {
          this.approvalRate = Math.round(
            (this.acceptedRequestsCount / this.totalRequestsCount) * 100
          );
        }

        // Events and locations
        this.eventsCount = results.events.length;
        this.locationsCount = results.locations.length;

        // Show statistics only if we have processed requests
        this.hasStatistics = this.totalRequestsCount > 0;

        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading statistics:', error);
        this.loading = false;
      },
    });
  }

  navigateToRequests() {
    this.router.navigate(['/admin/requests']);
  }

  navigateToLocations() {
    this.router.navigate(['/locations']);
  }

  navigateToEvents() {
    this.router.navigate(['/events']);
  }
}
