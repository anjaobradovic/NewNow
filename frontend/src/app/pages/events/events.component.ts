import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-neutral-50 py-12 px-4">
      <div class="max-w-7xl mx-auto">
        <div class="flex items-center justify-between mb-6">
          <h1 class="text-4xl font-bold text-neutral-900">Events</h1>
          <div class="flex gap-3">
            <a routerLink="/events/today" class="btn-secondary">Today's Events</a>
            <a routerLink="/events" class="btn-primary">Search Events</a>
          </div>
        </div>

        <p class="text-neutral-600">Choose an option above to continue.</p>
      </div>
    </div>
  `,
})
export class EventsComponent {}
