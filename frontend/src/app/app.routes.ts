import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { EventsComponent } from './pages/events/events.component';
import { LocationListComponent } from './pages/location-list/location-list.component';
import { LocationDetailsComponent } from './pages/location-details/location-details.component';
import { LocationNewComponent } from './pages/location-new/location-new.component';
import { LocationEditComponent } from './pages/location-edit/location-edit.component';
import { LocationManagersComponent } from './pages/location-managers/location-managers.component';
import { AdminRequestsComponent } from './pages/admin-requests/admin-requests.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';
import { MeComponent } from './pages/me/me.component';
import { MeEditComponent } from './pages/me-edit/me-edit.component';
import { MeChangePasswordComponent } from './pages/me-change-password/me-change-password.component';
import { MeReviewsComponent } from './pages/me-reviews/me-reviews.component';
import { MeManagedLocationsComponent } from './pages/me-managed-locations/me-managed-locations.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/register-request', component: RegisterComponent },
  { path: 'events', component: EventsComponent },
  { path: 'locations', component: LocationListComponent },
  { path: 'locations/new', component: LocationNewComponent, canActivate: [adminGuard] },
  { path: 'locations/:id', component: LocationDetailsComponent },
  { path: 'locations/:id/edit', component: LocationEditComponent, canActivate: [authGuard] },
  {
    path: 'locations/:id/managers',
    component: LocationManagersComponent,
    canActivate: [adminGuard],
  },
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [adminGuard],
  },
  {
    path: 'admin/requests',
    component: AdminRequestsComponent,
    canActivate: [adminGuard],
  },
  // Profile
  { path: 'me', component: MeComponent, canActivate: [authGuard] },
  { path: 'me/edit', component: MeEditComponent, canActivate: [authGuard] },
  { path: 'me/change-password', component: MeChangePasswordComponent, canActivate: [authGuard] },
  { path: 'me/reviews', component: MeReviewsComponent, canActivate: [authGuard] },
  {
    path: 'me/managed-locations',
    component: MeManagedLocationsComponent,
    canActivate: [authGuard],
  },

  { path: '**', redirectTo: '' },
];
