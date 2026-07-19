import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { unsavedChangesGuard } from './core/guards/unsaved-changes.guard';

/**
 * Routen-Definition. Öffentliche Seiten (Landing, Auth) und geschützte Bereiche
 * unter /app (App-Shell + authGuard).
 */
export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/landing/landing.component').then((m) => m.LandingComponent),
    title: 'Jobbed – Bewerbungen im Griff',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
    title: 'Anmelden – Jobbed',
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
    title: 'Registrieren – Jobbed',
  },
  {
    path: 'verify-email',
    loadComponent: () =>
      import('./features/auth/verify-email/verify-email.component').then(
        (m) => m.VerifyEmailComponent,
      ),
    title: 'E-Mail bestätigen – Jobbed',
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password.component').then(
        (m) => m.ForgotPasswordComponent,
      ),
    title: 'Passwort vergessen – Jobbed',
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password.component').then(
        (m) => m.ResetPasswordComponent,
      ),
    title: 'Passwort zurücksetzen – Jobbed',
  },
  {
    path: 'privacy',
    loadComponent: () =>
      import('./features/legal/privacy.component').then((m) => m.PrivacyComponent),
    title: 'Datenschutz – Jobbed',
  },
  {
    path: 'imprint',
    loadComponent: () =>
      import('./features/legal/imprint.component').then((m) => m.ImprintComponent),
    title: 'Impressum – Jobbed',
  },
  {
    path: 'app',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
        title: 'Dashboard – Jobbed',
      },
      {
        path: 'applications',
        loadComponent: () =>
          import('./features/applications/application-list.component').then(
            (m) => m.ApplicationListComponent,
          ),
        title: 'Bewerbungen – Jobbed',
      },
      {
        path: 'board',
        loadComponent: () =>
          import('./features/kanban/kanban-board.component').then((m) => m.KanbanBoardComponent),
        title: 'Kanban-Board – Jobbed',
      },
      {
        path: 'calendar',
        loadComponent: () =>
          import('./features/calendar/calendar.component').then((m) => m.CalendarComponent),
        title: 'Interviews & Erinnerungen – Jobbed',
      },
      {
        path: 'job-analysis',
        loadComponent: () =>
          import('./features/job-analysis/job-analysis.component').then(
            (m) => m.JobAnalysisComponent,
          ),
        title: 'Stellenanalyse – Jobbed',
      },
      {
        path: 'resume',
        loadComponent: () =>
          import('./features/resume/resume.component').then((m) => m.ResumeComponent),
        title: 'Lebenslauf erstellen – Jobbed',
      },
      {
        path: 'applications/new',
        loadComponent: () =>
          import('./features/applications/application-form.component').then(
            (m) => m.ApplicationFormComponent,
          ),
        canDeactivate: [unsavedChangesGuard],
        title: 'Neue Bewerbung – Jobbed',
      },
      {
        path: 'applications/:id',
        loadComponent: () =>
          import('./features/applications/application-detail.component').then(
            (m) => m.ApplicationDetailComponent,
          ),
        title: 'Bewerbung – Jobbed',
      },
      {
        path: 'applications/:id/edit',
        loadComponent: () =>
          import('./features/applications/application-form.component').then(
            (m) => m.ApplicationFormComponent,
          ),
        canDeactivate: [unsavedChangesGuard],
        title: 'Bewerbung bearbeiten – Jobbed',
      },
      {
        path: 'companies',
        loadComponent: () =>
          import('./features/companies/company-list.component').then((m) => m.CompanyListComponent),
        title: 'Unternehmen – Jobbed',
      },
      {
        path: 'companies/new',
        loadComponent: () =>
          import('./features/companies/company-form.component').then((m) => m.CompanyFormComponent),
        canDeactivate: [unsavedChangesGuard],
        title: 'Neues Unternehmen – Jobbed',
      },
      {
        path: 'companies/:id/edit',
        loadComponent: () =>
          import('./features/companies/company-form.component').then((m) => m.CompanyFormComponent),
        canDeactivate: [unsavedChangesGuard],
        title: 'Unternehmen bearbeiten – Jobbed',
      },
      {
        path: 'contacts',
        loadComponent: () =>
          import('./features/contacts/contact-list.component').then((m) => m.ContactListComponent),
        title: 'Kontakte – Jobbed',
      },
      {
        path: 'contacts/new',
        loadComponent: () =>
          import('./features/contacts/contact-form.component').then((m) => m.ContactFormComponent),
        canDeactivate: [unsavedChangesGuard],
        title: 'Neuer Kontakt – Jobbed',
      },
      {
        path: 'contacts/:id/edit',
        loadComponent: () =>
          import('./features/contacts/contact-form.component').then((m) => m.ContactFormComponent),
        canDeactivate: [unsavedChangesGuard],
        title: 'Kontakt bearbeiten – Jobbed',
      },
    ],
  },
  {
    path: '**',
    loadComponent: () =>
      import('./features/not-found/not-found.component').then((m) => m.NotFoundComponent),
    title: 'Seite nicht gefunden – Jobbed',
  },
];
