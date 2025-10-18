import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="min-h-screen bg-neutral-50 py-12 px-4">
      <div class="max-w-7xl mx-auto">
        <h1 class="text-4xl font-bold text-neutral-900 mb-4">Events</h1>
        <p class="text-neutral-600">This page will be implemented in the next phase.</p>
      </div>
    </div>
  `,
})
export class EventsComponent {}
