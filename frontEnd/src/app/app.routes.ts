import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { FileBrowserComponent } from './components/file-browser/file-browser.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'files', component: FileBrowserComponent, canActivate: [authGuard] },
  {
    path: 'files/:folderId',
    component: FileBrowserComponent,
    canActivate: [authGuard],
  },
  { path: '', redirectTo: 'files', pathMatch: 'full' },
  { path: '**', redirectTo: 'files' },
];
