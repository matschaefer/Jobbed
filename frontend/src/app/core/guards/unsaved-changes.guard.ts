import { CanDeactivateFn } from '@angular/router';

/** Komponenten, die vor Verlassen bei ungespeicherten Änderungen warnen wollen. */
export interface CanComponentDeactivate {
  canDeactivate: () => boolean;
}

/**
 * Warnt vor Datenverlust, wenn ein Formular ungespeicherte Änderungen enthält.
 */
export const unsavedChangesGuard: CanDeactivateFn<CanComponentDeactivate> = (component) => {
  if (component.canDeactivate && !component.canDeactivate()) {
    return confirm('Es gibt ungespeicherte Änderungen. Möchtest du die Seite wirklich verlassen?');
  }
  return true;
};
