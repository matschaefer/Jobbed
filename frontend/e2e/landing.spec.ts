import { test, expect } from '@playwright/test';

// Grundlegendes Smoke-Szenario. Die vollständigen E2E-Szenarien (Registrierung,
// Weitere Kernabläufe werden in den jeweiligen E2E-Spezifikationen geprüft.
test.describe('Landing Page', () => {
  test('zeigt Titel und Call-to-Action', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Kostenlos starten' })).toBeVisible();
  });
});
