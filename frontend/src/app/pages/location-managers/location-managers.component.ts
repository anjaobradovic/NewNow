import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { LocationService } from '../../services/location.service';
import { ManagerDTO } from '../../models/location.model';

@Component({
  selector: 'app-location-managers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-autumn-cream via-neutral-50 to-white">
      <section class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-neutral-900">Location managers</h1>
          <p class="text-neutral-600 mt-1">Assign or remove managers</p>
        </div>

        <div class="card p-6 mb-6 space-y-4">
          <div>
            <label class="block text-sm font-medium text-neutral-700 mb-2">Search user</label>
            <input
              class="input-field"
              [(ngModel)]="query"
              (ngModelChange)="queueSearch()"
              placeholder="Type at least 1 character (name or email)"
            />
          </div>

          <div *ngIf="loadingSearch" class="text-sm text-neutral-500">Searching...</div>
          <div *ngIf="searchError" class="text-sm text-red-600">Search failed. Try again.</div>

          <div
            *ngIf="!loadingSearch && candidates().length > 0"
            class="border border-neutral-100 rounded-2xl divide-y max-h-60 overflow-auto"
          >
            <button
              type="button"
              class="w-full text-left px-4 py-3 flex items-center justify-between transition-colors"
              *ngFor="let u of candidates()"
              (click)="select(u)"
              [attr.aria-pressed]="selectedUser?.id === u.id"
              [class.bg-primary-50]="selectedUser?.id === u.id"
              [class.text-primary-800]="selectedUser?.id === u.id"
              [class.border]="selectedUser?.id === u.id"
              [class.border-primary-200]="selectedUser?.id === u.id"
              [class.hover:bg-neutral-50]="selectedUser?.id !== u.id"
            >
              <div>
                <div class="font-medium">{{ u.name || u.email }}</div>
                <div
                  class="text-sm text-neutral-600"
                  [class.text-primary-700]="selectedUser?.id === u.id"
                >
                  {{ u.email }}
                </div>
              </div>
              <div class="flex items-center gap-2">
                <span
                  class="text-xs text-neutral-500"
                  [class.text-primary-700]="selectedUser?.id === u.id"
                  >ID: {{ u.id }}</span
                >
                <svg
                  *ngIf="selectedUser?.id === u.id"
                  class="w-4 h-4 text-primary-700"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                </svg>
              </div>
            </button>
          </div>
          <p
            class="text-sm text-neutral-500"
            *ngIf="!loadingSearch && !searchError && query.length >= 1 && candidates().length === 0"
          >
            No users found.
          </p>

          <div class="flex justify-end">
            <button
              class="btn-primary"
              (click)="assignSelected()"
              [disabled]="assigning || !selectedUser"
            >
              {{ assigning ? 'Assigning...' : 'Assign' }}
            </button>
          </div>
        </div>

        <div class="card p-6">
          <h3 class="text-lg font-semibold mb-4">Current managers</h3>
          <div class="divide-y divide-neutral-100">
            <div class="py-3 flex items-center justify-between" *ngFor="let m of managers()">
              <div>
                <div class="font-medium">{{ m.name }}</div>
                <div class="text-sm text-neutral-600">{{ m.email }}</div>
                <div class="text-xs text-neutral-500">
                  From {{ m.startDate || '—' }} to {{ m.endDate || '—' }}
                </div>
              </div>
              <div class="flex items-center gap-3">
                <span
                  class="px-2 py-1 rounded-full text-xs"
                  [class.bg-primary-100]="m.active"
                  [class.text-primary-700]="m.active"
                  [class.bg-neutral-200]="!m.active"
                  [class.text-neutral-700]="!m.active"
                  >{{ m.active ? 'Active' : 'Inactive' }}</span
                >
                <button
                  class="btn-secondary"
                  (click)="remove(m.userId)"
                  [disabled]="removingId === m.userId"
                >
                  {{ removingId === m.userId ? 'Removing...' : 'Remove' }}
                </button>
              </div>
            </div>
            <div *ngIf="managers().length === 0" class="text-sm text-neutral-500 py-6 text-center">
              No managers assigned.
            </div>
          </div>
        </div>
      </section>
    </div>
  `,
})
export class LocationManagersComponent implements OnInit {
  managers = signal<ManagerDTO[]>([]);
  query = '';
  candidates = signal<Array<{ id: number; name: string; email: string }>>([]);
  selectedUser: { id: number; name: string; email: string } | null = null;
  loadingSearch = false;
  searchError = false;
  assigning = false;
  removingId: number | null = null;
  private id!: number;
  private searchTimer: any;

  constructor(
    private route: ActivatedRoute,
    private service: LocationService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.reload();
  }

  reload(): void {
    this.service.getManagers(this.id).subscribe({
      next: (res) => this.managers.set(res || []),
      error: () => this.toastr.error('Failed to load managers'),
    });
  }

  queueSearch(): void {
    this.searchError = false;
    this.selectedUser = null;
    if (this.searchTimer) clearTimeout(this.searchTimer);
    const q = this.query.trim();
    if (q.length < 1) {
      this.candidates.set([]);
      return;
    }
    this.searchTimer = setTimeout(() => this.search(q), 400);
  }

  private search(q: string): void {
    this.loadingSearch = true;
    this.service.searchUsers(q).subscribe({
      next: (users: any) => {
        const lower = q.toLowerCase();
        const filtered = (users || []).filter(
          (u: any) =>
            (u.name || '').toLowerCase().includes(lower) ||
            (u.email || '').toLowerCase().includes(lower)
        );
        this.candidates.set(filtered.slice(0, 10));
        this.loadingSearch = false;
      },
      error: () => {
        this.loadingSearch = false;
        this.candidates.set([]);
        this.searchError = true;
      },
    });
  }

  select(u: { id: number; name: string; email: string }): void {
    this.selectedUser = u;
  }

  assignSelected(): void {
    if (!this.selectedUser) return;
    this.assigning = true;
    this.service.assignManager(this.id, this.selectedUser.id).subscribe({
      next: (res) => {
        this.toastr.success(res.message || 'Manager assigned');
        this.query = '';
        this.candidates.set([]);
        this.selectedUser = null;
        this.reload();
      },
      error: (err) => this.toastr.error(err?.error?.message || 'Failed to assign manager'),
      complete: () => (this.assigning = false),
    });
  }

  remove(userId: number): void {
    this.removingId = userId;
    this.service.removeManager(this.id, userId).subscribe({
      next: (res) => {
        this.toastr.success(res.message || 'Manager removed');
        this.reload();
      },
      error: (err) => this.toastr.error(err?.error?.message || 'Failed to remove manager'),
      complete: () => (this.removingId = null),
    });
  }
}
