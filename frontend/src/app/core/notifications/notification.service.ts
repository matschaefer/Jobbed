import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/** Zentrale Toast-Benachrichtigungen über Angular Material Snackbar. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  success(message: string): void {
    this.show(message, 'jt-snack-success');
  }

  error(message: string): void {
    this.show(message, 'jt-snack-error');
  }

  info(message: string): void {
    this.show(message, 'jt-snack-info');
  }

  private show(message: string, panelClass: string): void {
    this.snackBar.open(message, 'OK', {
      duration: 5000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: [panelClass],
    });
  }
}
