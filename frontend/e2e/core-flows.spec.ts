import { BrowserContext, Page, test, expect } from '@playwright/test';

test.describe.serial('Jobbed Kernabläufe', () => {
  let context: BrowserContext;
  let page: Page;

  test.beforeAll(async ({ browser }) => {
    context = await browser.newContext();
    page = await context.newPage();
    await page.goto('/login');
    await page.locator('input[type="email"]').fill('analytics@jobbed.local');
    await page.locator('input[type="password"]').fill('Str0ng!Passw0rd');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('**/app/dashboard');
  });

  test.afterAll(async () => { await context.close(); });

  test('1 – Demo-Login öffnet das Dashboard', async () => {
    await page.goto('/app/dashboard');
    await expect(page.locator('app-dashboard')).toBeVisible();
  });

  test('2 – Kanban lädt Statusspalten und Demo-Karten', async () => {
    await page.goto('/app/board');
    await expect(page.locator('.cdk-drop-list')).toHaveCount(12);
    await expect(page.locator('.cdk-drag')).toHaveCount(8);
  });

  test('3 – Bewerbungsliste ist erreichbar', async () => {
    await page.goto('/app/applications');
    await expect(page.locator('app-application-list')).toBeVisible();
    await expect(page.getByText('Backend Engineer', { exact: true })).toBeVisible();
  });

  test('4 – Kalender rendert einen vollständigen Monatsraster', async () => {
    await page.goto('/app/calendar');
    await expect(page.locator('app-calendar .calendar-day')).toHaveCount(42);
    await expect(page.locator('app-calendar mat-progress-bar')).toHaveCount(0);
  });

  test('5 – Stellenanzeige wird analysiert', async () => {
    await page.goto('/app/job-analysis');
    await page.getByRole('button', { name: /Beispiel einsetzen/ }).click();
    await page.locator('app-job-analysis button.btn-primary').click();
    await expect(page.getByText('43%', { exact: true })).toBeVisible();
    await expect(page.getByText(/Regelbasierte Analyse|KI-Analyse/)).toBeVisible();
  });

  test('6 – Lebenslauf-Entwurf wird erstellt', async () => {
    await page.goto('/app/resume');
    await page.locator('textarea[formcontrolname="experience"]').fill('TechCorp: Backend-Services entwickelt');
    await page.locator('input[formcontrolname="targetRole"]').fill('Platform Engineer');
    await page.locator('app-resume form button[type="submit"]').click();
    await expect(page.locator('.resume-paper')).toBeVisible();
    await expect(page.getByRole('button', { name: /Drucken \/ PDF/ })).toBeVisible();
  });

  test('7 – Unternehmen sind nutzergebunden erreichbar', async () => {
    await page.goto('/app/companies');
    await expect(page.locator('app-company-list')).toBeVisible();
    await expect(page.getByText('TechCorp', { exact: true }).first()).toBeVisible();
  });

  test('8 – Kontakte-Seite lädt ohne Fehler', async () => {
    await page.goto('/app/contacts');
    await expect(page.locator('app-contact-list')).toBeVisible();
  });

  test('9 – fremde Bewerbungen bleiben unsichtbar', async ({ request }) => {
    const ownerLogin = await request.post('/api/v1/auth/login', { data: { email: 'analytics@jobbed.local', password: 'Str0ng!Passw0rd' } });
    const intruderLogin = await request.post('/api/v1/auth/login', { data: { email: 'e2e-security@jobbed.local', password: 'Str0ng!Passw0rd' } });
    expect(ownerLogin.ok()).toBeTruthy(); expect(intruderLogin.ok()).toBeTruthy();
    const ownerToken = (await ownerLogin.json()).accessToken as string;
    const intruderToken = (await intruderLogin.json()).accessToken as string;
    const ownerList = await request.get('/api/v1/applications?page=0&size=1', { headers: { Authorization: `Bearer ${ownerToken}` } });
    const foreignId = (await ownerList.json()).content[0].id as string;
    const foreignRead = await request.get(`/api/v1/applications/${foreignId}`, { headers: { Authorization: `Bearer ${intruderToken}` } });
    expect(foreignRead.status()).toBe(404);
  });
});
