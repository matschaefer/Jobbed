import { TestBed } from '@angular/core/testing';
import { CanComponentDeactivate, unsavedChangesGuard } from './unsaved-changes.guard';

describe('unsavedChangesGuard', () => {
  function run(component: CanComponentDeactivate): boolean {
    return TestBed.runInInjectionContext(
      () => unsavedChangesGuard(component, {} as never, {} as never, {} as never) as boolean,
    );
  }

  it('allows navigation when there are no unsaved changes', () => {
    expect(run({ canDeactivate: () => true })).toBeTrue();
  });

  it('asks for confirmation when there are unsaved changes', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    expect(run({ canDeactivate: () => false })).toBeTrue();
    expect(window.confirm).toHaveBeenCalled();
  });

  it('blocks navigation when confirmation is declined', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    expect(run({ canDeactivate: () => false })).toBeFalse();
  });
});
